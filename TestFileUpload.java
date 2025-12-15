import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class TestFileUpload {
    public static void main(String[] args) {
        try {
            File file = new File("test-upload.txt");
            if (!file.exists()) {
                System.err.println("Test file not found: " + file.getAbsolutePath());
                return;
            }

            System.out.println("Testing multipart/form-data upload...");
            testUpload("multipart/form-data", file);

            System.out.println("\nTesting multipart/mixed upload...");
            testUpload("multipart/mixed", file);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void testUpload(String contentType, File file) throws Exception {
        URL url = new URL("http://localhost:10086/api/files");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.setRequestMethod("POST");

        String boundary = "----WebKitFormBoundary" + System.currentTimeMillis();
        connection.setRequestProperty("Content-Type", contentType + "; boundary=" + boundary);

        try (DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream())) {
            outputStream.writeBytes("--" + boundary + "\r\n");
            outputStream.writeBytes("Content-Disposition: form-data; name=\"file\"; filename=\"" + file.getName() + "\"\r\n");
            outputStream.writeBytes("Content-Type: text/plain\r\n");
            outputStream.writeBytes("\r\n");

            try (FileInputStream fileInputStream = new FileInputStream(file)) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            }

            outputStream.writeBytes("\r\n--" + boundary + "--\r\n");
            outputStream.flush();
        }

        int responseCode = connection.getResponseCode();
        System.out.println("Response Code: " + responseCode);

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            String line;
            StringBuilder response = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            System.out.println("Response Body: " + response.toString());
        } catch (IOException e) {
            try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(connection.getErrorStream()))) {
                String line;
                StringBuilder errorResponse = new StringBuilder();
                while ((line = errorReader.readLine()) != null) {
                    errorResponse.append(line);
                }
                System.err.println("Error Response: " + errorResponse.toString());
            }
        }

        connection.disconnect();
    }
}