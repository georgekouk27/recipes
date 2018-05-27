package gr.georkouk.recipes.utils;

import android.content.Context;

import java.io.InputStream;
import java.util.Scanner;

public class Json {

    public static String readJsonFile(Context context){
        try {
            String jsonContent = null;

            InputStream inputStream = context.getAssets().open("recipes.json");
            Scanner scanner = new Scanner(inputStream);
            scanner.useDelimiter("\\A");

            boolean hasInput = scanner.hasNext();
            if (hasInput) jsonContent = scanner.next();

            inputStream.close();
            return jsonContent;
        }
        catch (Exception ignore) {
            return null;
        }
    }

}
