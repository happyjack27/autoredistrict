package excel;

import java.util.Vector;
import jp.ne.so_net.ga2.no_ji.jcom.IDispatch;
import jp.ne.so_net.ga2.no_ji.jcom.ReleaseManager;
//import com.jacob.activeX.ActiveXComponent;
//import com.jacob.com.*;

public class ExcelObj extends Dispatch{
	public Dispatch d = null;
	public Dispatch app = null;
	public ExcelObj parent = null;
	Vector subs = new Vector();
	static int cell_dispid;
	static int value_dispid;
	ReleaseManager rm = null;
	IDispatch superid = null;
	// CONSTRUCTORS
	ExcelObj(){
	}
	ExcelObj(Dispatch d){
		this.d = d;
	}
	ExcelObj(String s){
		init();
		d = Workbooks().Open(s).d;
	}
	// try { return new Location( (IDispatch)d.method("Item",new Object[]{new Integer(i)})); } catch (Exception e) { System.out.println("e "+e); e.printStackTrace(); return null; } }
	/*
	 * ReleaseManager rm = new ReleaseManager(); MapPointApplication() { try { d = new IDispatch(rm, "MapPoint.Application"); } catch (Exception e) { System.out.println("e "+e); e.printStackTrace(); } } public Map getMap() { try { return new Map((IDispatch)d.get( "ActiveMap")); } catch (Exception e) { System.out.println("e "+e); e.printStackTrace(); return null; } } public Map newMap() { try { return new Map((IDispatch)d.method("NewMap",null)); } catch (Exception e) { System.out.println("e "+e); e.printStackTrace(); return null; } } public void Quit() { System.out.println( "ending App"); try { d.method("Quit", null); d.release(); } catch (Exception e) { System.out.println("couldn't release:"+e); return; } try { rm.release(); } catch (Exception e) { System.out.println("couldn't release:"+e); return; } System.out.println( "App ended."); }
	 */
	// MISC
	public int getCount(){
		return get(d, "Count").toInt();
	}
	// APPS
	public void init(){
		d = app = new ActiveXComponent("Excel.Application");
	}
	public void setVisible(boolean b){
		d.put(d, "Visible", new Variant(b));
	}
	public ExcelObj Workbooks(){
		return addsub(get(d, "Workbooks").toDispatch());
	}
	public ExcelObj New(){
		init();
		return Workbooks().Add();
	}
	public ExcelObj New(String s){
		init();
		return Workbooks().Add(s);
	}
	public void Print(){
		call(call(app, "Dialogs", new Variant(8)).toDispatch(), "Show");
	}
	public void Calculate(){
		call(app, "Calculate");
	}
	public void Quit(){
		System.out.println("ending Excel App");
		// releaseAll();
		if(app == null){
			app = d;
		}
		// app.invoke("Quit", new Variant[]{});
		// d.release();
		try{
			app.call(app, "Quit");
			// ComThread.Release();
		}
		catch(Exception e){
			System.out.println("couldn't release:" + e);
			return;
		}
		rm.release();
		System.out.println("Excel App ended.");
	}
	// WORKBOOKS
	public ExcelObj Open(String s){
		return addsub(call(d, "Open", new Variant(s)).toDispatch());
	}
	public ExcelObj Add(){
		return addsub(call(d, "Add").toDispatch());
	}
	public ExcelObj Add(String s){
		return addsub(call(d, "Add", new Variant(s)).toDispatch());
	}
	public void Save(){
		call(d, "Save");
	}
	public void SaveAs(String s){
		call(d, "SaveAs", new Variant(s));
	}
	public void Close(){
		Close(false);
	}
	public void Close(boolean b){
		call(d, "Close", new Variant(b));
		if(app != null){
			Quit();
		}
	}
	public ExcelObj Worksheets(String s){
		ExcelObj ws = addsub(call(d, "Worksheets", new Variant(s)).toDispatch());
		return ws;
	}
	public ExcelObj Worksheets(int i){
		ExcelObj ws = addsub(call(d, "Worksheets", new Variant(i)).toDispatch());
		return ws;
	}
	// WORKSHEETS
	public ExcelObj Columns(){
		return addsub(call(d, "Columns").toDispatch());
	}
	public ExcelObj Columns(int i){
		return addsub(call(d, "Columns", new Variant(i)).toDispatch());
	}
	public void PrintNoDialog(){
		call(d, "PrintOut");
	}
	// COLUMNS
	public void AutoFit(){
		call(d, "AutoFit");
	}
	public void setColumnWidth(int i){
		put(d, "ColumnWidth", new Variant(i));
	}
	public ExcelObj Rows(){
		return addsub(call(d, "Rows").toDispatch());
	}
	public ExcelObj Rows(int i){
		return addsub(call(d, "Rows", new Variant(i)).toDispatch());
	}
	// ROWS
	public void setRowHeight(int i){
		put(d, "RowHeight", new Variant(i));
	}
	public ExcelObj Cells(int i, int j){
		return new ExcelObj(call(d, "Cells", new Variant(i), new Variant(j)).toDispatch());
	}
	public ExcelObj Range(String s){
		return addsub(call(d, "Range", new Variant(s)).toDispatch());
	}
	// CELLS OR RANGE
	public void setValue(Variant v){
		put(d, "Value", v);
	}
	public void setValue(String s){
		put(d, "Value", new Variant(s));
	}
	public void setValue(int i){
		put(d, "Value", new Variant(i));
	}
	public Variant getValue(){
		return get(d, "Value");
	}
	public Variant Value(){
		return get(d, "Value");
	}
	public void setFormula(String s){
		put(d, "Formula", new Variant(s));
	}
	public Variant getFormula(){
		return get(d, "Formula");
	}
	public Variant Formula(){
		return get(d, "Formula");
	}
	@Override
	public String toString(){
		return get(d, "Value").toString();
	}
	public void setNumberFormat(String v){
		put(d, "NumberFormat", new Variant(v));
	}
	public String getNumberFormat(){
		return get(d, "NumberFormat").toString();
	}
	public void setHorizontalAlignment(int i){
		put(d, "HorizontalAlignment", new Variant(i));
	}
	public int getHorizontalAlignment(){
		return get(d, "HorizontalAlignment").toInt();
	}
	final static int ALIGN_LEFT = -4131, ALIGN_CENTER = -4108, ALIGN_RIGHT = -4152, ALIGN_GENERAL = 1;
	public void setInteriorColor(int i){
		put(call(d, "Interior").toDispatch(), "Color", new Variant(i));
	}
	public void setInteriorColorIndex(int i){
		put(call(d, "Interior").toDispatch(), "ColorIndex", new Variant(i));
	}
	public int getInteriorColor(){
		return get(call(d, "Interior").toDispatch(), "Color").toInt();
	}
	public int getInteriorColorIndex(){
		return get(call(d, "Interior").toDispatch(), "ColorIndex").toInt();
	}
	public ExcelObj Font(){
		return addsub(call(d, "Font").toDispatch());
	}
	// FONT
	public void setName(String s){
		put(d, "Name", new Variant(s));
	}
	public String getName(){
		return get(d, "Name").toString();
	}
	public void setSize(int i){
		put(d, "Size", new Variant(i));
	}
	public void setBold(boolean b){
		put(d, "Bold", new Variant(b));
	}
	public void setItalic(boolean b){
		put(d, "Italic", new Variant(b));
	}
	public void setColor(int i){
		put(d, "Color", new Variant(i));
	} // HEX: 0x00RRGGBB
	public int getColor(){
		return get(d, "Color").toInt();
	}
	public int getColorIndex(){
		return get(d, "ColorIndex").toInt();
	}
	public ExcelObj Borders(){
		return addsub(call(d, "Borders").toDispatch());
	}
	public ExcelObj Borders(int i){
		return addsub(call(d, "Borders", new Variant(i)).toDispatch());
	} // 1 to 10
	final static int LEFT = 7, RIGHT = 10, TOP = 8, BOTTOM = 9, DIAG_DOWN = 5, DIAG_UP = 6;
	// BORDERS
	public void setWeight(int i){
		put(d, "Weight", new Variant(i));
	}
	public int getWeight(){
		return get(d, "Weight").toInt();
	}
	final static int NORMAL = 2, LIGHT = 1, MEDUIM = -4138, HEAVY = 4;
	public void setLineStyle(int i){
		put(d, "LineStyle", new Variant(i));
	}
	public int getLineStyle(){
		return get(d, "LineStyle").toInt();
	}
	final static int NONE = -4142, SOLID = 1, DOTTED = -4118, DASHED = -4115, DOUBLED = -4119;
	// SPECIAL
	public ExcelObj addsub(Dispatch d0){
		ExcelObj newsub = new ExcelObj(d0);
		newsub.parent = this;
		subs.add(newsub);
		return newsub;
	}
	public void releaseAll(){
		for(int i = 0; i < subs.size(); i++){
			((ExcelObj) subs.get(i)).releaseAll();
			// ((ExcelObj) subs.get(i)).d.release();
		}
	}
	class ActiveXComponent extends Dispatch{
		ActiveXComponent(String s){
			try{
				rm = new ReleaseManager();
				id = new IDispatch(rm, s);
				if(id == null){
					System.out.println("jcom ex on ActiveXComponent " + s + ": ID IS NULL!!");
				}
				superid = id;
			}
			catch(Exception ex){
				System.out.println("jcom ex on ActiveXComponent " + s + ": " + ex);
				ex.printStackTrace();
			}
		}
	}
}
