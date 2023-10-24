package excel;


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
			ex.printStackTrace();
			return new Dispatch();
		}
	}
}
