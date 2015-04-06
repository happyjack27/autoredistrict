package solutions;

import serialization.ReflectionJSONObject;
import ui.Applet;

public class Test extends ReflectionJSONObject<Test> {
	public int testmember;
    public static void main( String[] args ) {
    	Test t = new Test();
    	//JSONObject._
    	t.fromJSON("testmember: 4");
    	System.out.println("value: "+t.testmember);
    	System.out.println("value2: "+t.get("testmember"));
	}
	
	public Class getJSONClass() {
		return Test.class;
	}
    
}
