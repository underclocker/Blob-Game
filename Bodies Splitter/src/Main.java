import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class Main {
	public static void main(String[] args) {
		String text = null;
		try {
			text = new Scanner( new File("bodies.json") ).useDelimiter("\\A").next();
		} catch (Exception e) {
			System.out.println("err1");
			return;
		}
		
		JSONObject json = null;
		try {
			json = new JSONObject(text);
		} catch (JSONException e) {
			System.out.println("err2");
			return;
		}
				
		JSONArray rigidBodies = null;
		try {
			rigidBodies = json.getJSONArray("rigidBodies");
		} catch (JSONException e) {
			System.out.println("err3");
			return;
		}
		
		int len = rigidBodies.length();
		System.out.println(len);
		for (int i = 0; i < len; i++) {
			JSONObject body = null;
			try {
				body = rigidBodies.getJSONObject(i);
			} catch (JSONException e) {
				System.out.println("err4" + e);
				return;
			}
			
			String name = null;
			try {
				name = body.getString("name");
			} catch (JSONException e) {
				System.out.println("err5");
				return;
			}
			
			// Duplicate the original json
			JSONObject dup = null;
			try {
				dup = new JSONObject(json.toString());
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			ArrayList<Integer> toRemove = new ArrayList<Integer>();
			
			JSONArray tmpRigidBodies = null;
			try {
				tmpRigidBodies = dup.getJSONArray("rigidBodies");
			} catch (JSONException e) {
				System.out.println("err3");
				return;
			}
			
			int tmpLen = tmpRigidBodies.length();
			System.out.println("\t" + tmpLen);
			for (int j = 0; j < tmpLen; j++) {
				JSONObject tmpBody = null;
				try {
					tmpBody = tmpRigidBodies.getJSONObject(j);
				} catch (JSONException e) {
					System.out.println("err6" + e);
					return;
				}
				
				String tmpName = null;
				try {
					tmpName = tmpBody.getString("name");
				} catch (JSONException e) {
					System.out.println("err7");
					return;
				}
				
				if (!name.equals(tmpName)) {
					toRemove.add(j);
				}
			}
			
			
			for (int j = toRemove.size()-1; j >= 0; j--) {
				tmpRigidBodies.remove(toRemove.get(j));
			}
			
			// Print out
			String out = dup.toString();
			
			FileWriter outFile = null; 
			try {
				outFile = new FileWriter(name + ".json");
				outFile.write(out);
				outFile.close();
			} catch (IOException e) {
				System.out.println("err8");
				return;
			}
			
		}

	}
}
