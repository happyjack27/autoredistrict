package excel;

//import com.jacob.com.Variant;
public class ExcelTemplate{
	public ExcelTemplate(){
	}
	public int[] col_width;
	public int[] row_width;
	public String[][] font;
	public int[][] font_size;
	public boolean[][] bold;
	public boolean[][] italic;
	public boolean[][] isFormula;
	public String[][] number_format;
	public String[][] text;
	public int[][] color;
	public int[][] halign;
	public int[][][] border_style;
	public int[][][] border_weight;
	public void define(
			int[] col_width,
			int[] row_width,
			String[][] font,
			int[][] font_size,
			boolean[][] bold,
			boolean[][] italic,
			String[][] number_format,
			String[][] text,
			int[][] color,
			int[][] halign,
			int[][][] border_style,
			int[][][] border_weight
			){
		this.col_width = col_width;
		this.row_width = row_width;
		this.font = font;
		this.font_size = font_size;
		this.bold = bold;
		this.italic = italic;
		this.number_format = number_format;
		this.text = text;
		this.color = color;
		this.halign = halign;
		this.border_style = border_style;
		this.border_weight = border_weight;
	}
	public void write(ExcelObj ws){
		System.out.println(" writting excel template...");
		int i, j;
		ExcelObj cell;
		ExcelObj cfont;
		int rows;
		int cols;
		rows = text.length;
		cols = text[0].length;
		System.out.println(" writting excel template...2");
		try{
			for(i = 0; i < rows; i++){
				for(j = 0; j < cols - 1; j++){
					cell = ws.Cells(i + 1, j + 1);
					cfont = cell.Font();
					cfont.setName(font[i][j]);
					cfont.setSize(font_size[i][j]);
					cfont.setBold(bold[i][j]);
					cfont.setItalic(italic[i][j]);
					cell.setHorizontalAlignment(halign[i][j]);
					cell.put(cell.d, "NumberFormat", new Variant(number_format[i][j]));
					cell.setFormula(text[i][j]);
					cell.setInteriorColorIndex(color[i][j]);
					// cell.Borders(cell.LEFT).setWeight(border_weight[i][j][0]);
					// cell.Borders(cell.TOP).setWeight(border_weight[i][j][1]);
					// cell.Borders(cell.RIGHT).setWeight(border_weight[i][j][2]);
					// cell.Borders(cell.BOTTOM).setWeight(border_weight[i][j][3]);
					cell.Borders(ExcelObj.LEFT).setLineStyle(border_style[i][j][0]);
					cell.Borders(ExcelObj.TOP).setLineStyle(border_style[i][j][1]);
					cell.Borders(ExcelObj.RIGHT).setLineStyle(border_style[i][j][2]);
					cell.Borders(ExcelObj.BOTTOM).setLineStyle(border_style[i][j][3]);
				}
			}
		}
		catch(Exception f){
			System.out.println("ex:" + f.toString());
			f.printStackTrace();
		}
		try{
			for(i = 0; i < cols; i++){
				ws.Columns(i + 1).setColumnWidth(col_width[i]);
			}
			for(i = 0; i < rows; i++){
				ws.Rows(i + 1).setRowHeight(row_width[i]);
			}
		}
		catch(Exception f){
			System.out.println("ex:" + f.toString());
			f.printStackTrace();
		}
		System.out.println("finished writting excel template.");
	}
}