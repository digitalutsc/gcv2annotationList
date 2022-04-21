package gcv2anno;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import java.net.*;

public class gcv2anno {
    private static FileWriter file;

    @SuppressWarnings("unchecked")
    public static void main(String[] args) throws IOException, ParseException{
        final String INPUT_PATH = args[0];
        final String OUTPUT_PATH = args[1];
        final String CANVAS = args[2];

        // read the json file and parse it to a json object
        JSONParser parser = new JSONParser();
        JSONObject inJSONObject = (JSONObject) parser.parse(new FileReader(INPUT_PATH));

        // create new JSONObject to store the output
        JSONObject outJSONObject = new JSONObject();
        // store the context, id, and type to the output JSONObject
        outJSONObject.put("@context", "http://iiif.io/api/presentation/2/context.json");
        outJSONObject.put("@id", "https://islandora.traefik.me/" + OUTPUT_PATH);
        outJSONObject.put("@type", "sc:AnnotationList");
        // initialize resourcesArray (the array that stores the annotations)
        JSONArray resourcesArray = new JSONArray();

        // Use the input JSONObject to get the contents, height, width, and the x-y coordinates of the top left corner of the bounding box
        JSONArray pages = (JSONArray) ((JSONObject) inJSONObject.get("fullTextAnnotation")).get("pages");
        for(Object page: pages){
          JSONArray blocks = (JSONArray) ((JSONObject)page).get("blocks");
          for(Object block: blocks){
            JSONArray paragraphs = (JSONArray) ((JSONObject)block).get("paragraphs");
            for(Object paragraph: paragraphs){
              JSONArray vertices = (JSONArray)(((JSONObject)((JSONObject)paragraph).get("boundingBox"))).get("vertices");
              int i = 1;
              long x = 0;
              long y = 0;
              long xx = 0;
              long yy = 0;
              for(Object vertice: vertices){
                if(i==1){
                  // get the coordinates of the top left corner of the bounding box
                  x = (long) ((JSONObject)vertice).get("x");
                  y = (long) ((JSONObject)vertice).get("y");
                }else if(i==3){
                  // get the coordinates of the bottom right corner of the bounding box
                  xx = (long) ((JSONObject)vertice).get("x");
                  yy = (long) ((JSONObject)vertice).get("y");
                }
                i++;
              }
              // calculate the width by substracting the x coordinate of the bottom right corner with x coordinate of the top left corner
              long w = xx - x;
              // calculate the height by substracting the y coordinate of the bottom right corner with y coordinate of the top left corner
              long h = yy - y;

              // get the content
              String content = "";
              JSONArray words = (JSONArray) ((JSONObject)paragraph).get("words");
              for(Object word: words){
                JSONArray symbols = (JSONArray) ((JSONObject)word).get("symbols");
                for(Object symbol: symbols){
                  String text = (String) ((JSONObject)symbol).get("text");
                  content += text;
                  // add space between words if break detected
                  if(((JSONObject)symbol).get("property") != null){
                    JSONObject property = (JSONObject) ((JSONObject)symbol).get("property");
                      if(((String) ((JSONObject)((JSONObject)property).get("detectedBreak")).get("type")).equals("SPACE"))
                        content += ' ';
                  }
                }
              }

              // find a free port for the id of the annotation (passing 0 to ServerSocket constructor will make it listen on any free port.)
              // it doesn't matter which port is used as long as its free
              ServerSocket serverSocket = new ServerSocket(0);

              // add the information retrived to the resourcesObject
              JSONObject resourcesObject = new JSONObject();
              resourcesObject.put("@id", "http://localhost:" + serverSocket.getLocalPort() + "/annotation/"+ paragraph.hashCode());
              resourcesObject.put("@type", "oa:Annotation");
              resourcesObject.put("@context", "http://iiif.io/api/presentation/2/context.json");
              resourcesObject.put("on", CANVAS + "#xywh=" + x + "," + y + "," + w + "," + h);
              resourcesObject.put("label", content);
              JSONObject rObj = new JSONObject();
              rObj.put("@type", "dctypes:Text");
              rObj.put("http://dev.llgc.org.uk/sas/full_text", content);
              rObj.put("format", "text/html");
              rObj.put("chars", "<p>" + content + "</p>");
              resourcesObject.put("resource", rObj);
              JSONArray motivationObject = new JSONArray();
              motivationObject.add("oa:commenting");
              resourcesObject.put("motivation", motivationObject);
              
              // add the resourcesObject to the resourcesArray
              resourcesArray.add(resourcesObject);
            }
          }
        }
        // add the resourcesArray to the outJSONObject
        outJSONObject.put("resources", resourcesArray);

        // write the outJSONObject to a file
        try {
          // Constructs a FileWriter given a file name, using the platform's default charset
          file = new FileWriter(OUTPUT_PATH);
          file.write(outJSONObject.toJSONString().replace("\\/","/"));
        } catch (IOException e) {
          e.printStackTrace();
        } finally {
          try {
            file.flush();
            file.close();
          } catch (IOException e) {
            e.printStackTrace();
          }
        }
        System.out.println("done");
    }
}