package excel;

import jp.ne.so_net.ga2.no_ji.jcom.IDispatch;

/*

 public class Dispatch {
 IDispatch id = null;

 Dispatch() {}
 Dispatch(String s) {}
 Dispatch( IDispatch idd) { id=idd; }
 public Variant get(Dispatch d, String s) {
 IDispatch idd = d.id;
 if( idd == null) idd = id;
 try {
 return new Variant(idd.get(s));
 } catch (Exception ex) {
 System.out.println("jcom ex on get "+s+": "+ex);
 ex.printStackTrace();
 return new Variant();
 }
 }
 public void put(Dispatch d, String s, Variant v) {
 IDispatch idd = d.id;
 if( idd == null) idd = id;
 try {
 idd.put(s,v.o);
 } catch (Exception ex) {
 System.out.println("jcom ex on put "+s+": "+ex);
 ex.printStackTrace();
 }
 }


 }

 */
public class Dispatch{
	IDispatch id = null;
	Dispatch(){
	}
	Dispatch(String s){
	}
	Dispatch(IDispatch idd){
		id = idd;
	}
	public Variant get(Dispatch d, String s){
		IDispatch idd = d.id;
		if(idd == null){
			idd = id;
		}
		try{
			return new Variant(idd.get(s));
		}
		catch(Exception ex){
			System.out.println("jcom ex on get " + s + ": " + ex);
			ex.printStackTrace();
			return new Variant();
		}
	}
	public void put(Dispatch d, String s, Variant v){
		IDispatch idd = d.id;
		if(idd == null){
			idd = id;
		}
		try{
			idd.put(s, v.o);
		}
		catch(Exception ex){
			System.out.println("jcom ex on put " + s + ": " + ex);
			ex.printStackTrace();
		}
	}
	public Variant call(Dispatch d, String s){
		IDispatch idd = d.id;
		if(idd == null){
			idd = id;
		}
		try{
			return new Variant(idd.method(s, null));
		}
		catch(Exception e){
			try{
				return new Variant(idd.get(s, null));
			}
			catch(Exception ex){
				System.out.println("jcom ex on call0 " + s + ": " + ex);
				ex.printStackTrace();
				return new Variant();
			}
		}
	}
	public void put(Dispatch d, String s, String s2){
		put(d, s, new Variant(s2));
	}
	public Variant call(Dispatch d, String s, String s2){
		return call(d, s, new Variant(s2));
	}
	public Variant call(Dispatch d, String s, Variant v){
		IDispatch idd = d.id;
		if(idd == null){
			idd = id;
		}
		try{
			if(v.o instanceof Integer){
				return new Variant(idd.method(s, new Object[]{(Integer) v.o}));
			}
			else if(v.o instanceof Boolean){
				return new Variant(idd.method(s, new Object[]{(Boolean) v.o}));
			}
			else if(v.o instanceof String){
				return new Variant(idd.method(s, new Object[]{(String) v.o}));
			}
			else if(v.o instanceof IDispatch){
				return new Variant(idd.method(s, new Object[]{(IDispatch) v.o}));
			}
			return new Variant(idd.method(s, new Object[]{v.o}));
		}
		catch(Exception ex0){
			try{
				if(v.o instanceof Integer){
					return new Variant(idd.get(s, new Object[]{(Integer) v.o}));
				}
				else if(v.o instanceof Boolean){
					return new Variant(idd.get(s, new Object[]{(Boolean) v.o}));
				}
				else if(v.o instanceof String){
					return new Variant(idd.get(s, new Object[]{(String) v.o}));
				}
				else if(v.o instanceof IDispatch){
					return new Variant(idd.get(s, new Object[]{(IDispatch) v.o}));
				}
				return new Variant(idd.get(s, new Object[]{v.o}));
			}
			catch(Exception ex2){
				IDispatch dd;
				try{
					dd = (IDispatch) idd.get(s);
				}
				catch(Exception ex){
					System.out.println("jcom ex on call1(2) " + s + ": " + ex);
					ex.printStackTrace();
					return new Variant();
				}
				try{
					return new Variant(dd.get((String) v.o));// ,new Object[]{(String)v.o}));
				}
				catch(Exception ex){
					System.out.println("jcom ex on call1(3) " + v.o + ": " + ex);
					ex.printStackTrace();
					return new Variant();
				}
			}
		}
	}
	public Variant call(Dispatch d, String s, Variant v1, Variant v2){
		try{
			return new Variant(d.id.method(s, new Object[]{v1.o, v2.o}));
		}
		catch(Exception e){
			try{
				return new Variant(d.id.get(s, new Object[]{v1.o, v2.o}));
			}
			catch(Exception ex){
				System.out.println("jcom ex on call2 " + s + ": " + ex);
				ex.printStackTrace();
				return new Variant();
			}
		}
	}
	public Variant call(Dispatch d, String s, Variant[] vv){
		Object[] oo = new Object[vv.length];
		for(int i = 0; i < vv.length; i++){
			oo[i] = vv[i].o;
		}
		try{
			return new Variant(d.id.method(s, oo));
		}
		catch(Exception e){
			try{
				return new Variant(d.id.get(s, oo));
			}
			catch(Exception ex){
				System.out.println("jcom ex on call2 " + s + ": " + ex);
				ex.printStackTrace();
				return new Variant();
			}
		}
	}
}
