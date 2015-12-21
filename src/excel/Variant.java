package excel;

import jp.ne.so_net.ga2.no_ji.jcom.IDispatch;

public class Variant{
	public Object o = null;
	Variant(){
		o = null;
	}
	Variant(Object oo){
		o = oo;
	}
	Variant(int ii){
		o = new Integer(ii);
	}
	Variant(boolean bb){
		o = new Boolean(bb);
	}
	@Override
	public String toString(){
		if(o == null){
			return "null";
		}
		else{
			return o.toString();
		}
	}
	public int toInt(){
		return ((Integer) o).intValue();
	}
	public boolean toBoolean(){
		return ((Boolean) o).booleanValue();
	}
	public Dispatch toDispatch(){
		try{
			return new Dispatch((IDispatch) o);
		}
		catch(Exception ex){
			System.out.println("jcom ex on toDispatch : " + ex);
			ex.printStackTrace();
			return new Dispatch();
		}
	}
}
