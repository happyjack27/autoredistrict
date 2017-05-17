package util;

import serialization.ReflectionJSONObject;
import ui.*;

public class Test extends ReflectionJSONObject<Test> {
	public int testmember;
    public static void main( String[] args ) {
    	for( int i = 0; i < Download.states.length; i++) {
    		String fips = ""+i;
    		if( fips.length() < 2) {
    			fips = "0"+i;
    		}
    		String state = Download.states[i];
    		//String file = 
    		String path = "./"+state+"/2010/2012/vtd/tl_2012_"+fips+"_vtd10";
    		System.out.println("cp \""+path+".shp\" ./all/");
    		System.out.println("cp \""+path+".dbf\" ./all/");
    		System.out.println("cp \""+path+".prj\" ./all/");
    		System.out.println("cp \""+path+".shx\" ./all/");
    	}
    	/*
    	Test t = new Test();
    	//JSONObject._
    	t.fromJSON("testmember: 4");
    	System.out.println("value: "+t.testmember);
    	System.out.println("value2: "+t.get("testmember"));
    	*/
	}
	
	public Class getJSONClass() {
		return Test.class;
	}
}
