package org.example;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class ImageCropper {

    public static void imageCropper() {

        // Path to the JSON file
        String jsonPath = "object_class.json";

        // Path to the folder where the cropped images will be saved
        String outputPath = "objects_3/";

        // Read the JSON file and get the list of image objects
        ArrayList<JSONObject> images = readJSON(jsonPath);

        System.out.println("Script is starting! Will start crop images");
        // Crop each image and save it in the output folder
        for (JSONObject image : images) {
            try {
                cropImage(image, outputPath);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        System.out.println("Crop Images done. Starts to update Json!");

        // Update the JSON file with the new object URLs
        updateJSON(jsonPath, images);

        System.out.println("All Done!");
    }

    /**
     * Reads the JSON file and returns a list of image objects
     */
    private static ArrayList<JSONObject> readJSON(String jsonPath) {
        ArrayList<JSONObject> images = new ArrayList<>();

        JSONParser parser = new JSONParser();

        try {
            Object obj = parser.parse(new FileReader(jsonPath));

            JSONArray jsonArray = (JSONArray) obj;

            for (Object o : jsonArray) {
                JSONObject image = (JSONObject) o;
                images.add(image);
            }

        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }

        return images;
    }

    /**
     * Crops the image based on the coordinates in the JSON object and saves it in
     * the output folder
     */
    private static void cropImage(JSONObject image, String outputPath) throws IOException {
        // Get the image URL and crop coordinates from the JSON object
        String imageUrl = (String) image.get("image_thumbnail");
        String labelName = (String) image.get("label_text");
        Long id = (Long) image.get("index");
        JSONArray coordsArray = (JSONArray) image.get("coords");
        int[] coords = new int[coordsArray.size()];

        for (int i = 0; i < coordsArray.size(); i++) {
            coords[i] = ((Double) coordsArray.get(i)).intValue();
        }

        try {
            // Load the image from the URL
            URL url = new URL(imageUrl);
            BufferedImage fullImage = ImageIO.read(url);

            // Crop the image
            BufferedImage croppedImage = fullImage.getSubimage(coords[0], coords[1], coords[2] - coords[0],
                    coords[7] - coords[1]);

            // Save the cropped image in the output folder
            String outputFileName = (String) image.get("painting_id_back4app") + "_" + labelName + id + ".png";
            File outputFile = new File(outputPath + outputFileName);
            ImageIO.write(croppedImage, "png", outputFile);

            // Add the objectURL to the JSON
            String outputFileNamePath = outputPath + outputFileName;
            image.put("object_url", outputFileNamePath);

        } catch (Exception e) {
            // give me the id of the failing object
            System.out.print("SCRIPT ERROR OBEJCT INDEX: ");
            System.out.println(image.get("index"));

            // NEW CODE NOT TESTED!
            // add an empty string to "object_url" if it fails:;
            image.put("object_url", "");

            // continue to next image, if an error happen

        }

    }

    private static void updateJSON(String jsonPath, ArrayList<JSONObject> images) {
        JSONArray jsonArray = new JSONArray();

        for (JSONObject image : images) {
            jsonArray.add(image);
        }

        try {
            FileWriter fileWriter = new FileWriter(jsonPath);
            fileWriter.write(jsonArray.toJSONString());
            fileWriter.flush();
            fileWriter.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
