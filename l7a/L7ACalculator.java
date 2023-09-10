package lava7.l7a;
/**
* Java Calculator, modified from:
* https://dev.to/rohitk570/creating-a-calculator-using-java-awt-16ll
*
* This uses the L7AProcessor.
*/

import java.awt.*;
import java.awt.event.*;

public class L7ACalculator extends Frame implements ActionListener{
	//==========================
	static class WindowClosingListener extends WindowAdapter {
		public void windowClosing(WindowEvent evt) {
			System.out.println("window closing");
			System.exit(0);
		}
	}
	//==========================

	public static final String DIVISION_SIGN="\u00F7";
	public final static String MODEL="L7A";

	L7AProcessor p;

	//--------------------------------------------
	//Layout
	//Line 1: IN, text and Enter
	Label lblIn;
	TextField txtInput;
	Button bEnter;
	Button bRun;

	//Line 2:  stack
	Label lblS0;
	Label lblS1;

	//Line 3: output
	Label lblOut, lblMsg;

	//line 4
	Button bStore, bLoad; //, bNeg;
	Button bSqrt;

	//line 5:
	Button bA,bB,bC,bD,bOpen,bClose;

	//line 6: 7,8,9, FDIV
	Button b7,b8,b9,bfdiv;

	//line 7: 4,5,6, *,
	Button b4,b5,b6, bmul;

	//line 8: 1,2,3 -
	Button b1,b2,b3, bsub;

	//line 9: 0 . +/-
	Button b0, bpt, bsign, badd;

	//line 10:
	Button bclr, bback, bspace;

	//A,B,C registers for the local variables.  Off to the right
	Label lblA;
	Label lblB;
	Label lblC;
	Label lblD;

	public L7ACalculator(){
		super(MODEL);
		addWindowListener(new WindowClosingListener());
		p = new L7AProcessor();

		//line 1: INPUT
		//40 pixels down
		lblIn=new Label();
		lblIn.setBackground(Color.LIGHT_GRAY);
		lblIn.setBounds(50,40,30,20);
		lblIn.setText("IN");
		txtInput=new TextField(10);
		txtInput.setBounds(100,40,150,20);
		bEnter=new Button("ENTER");
		bEnter.setBounds(260,35,65,30);
		bRun=new Button("RUN");
		bRun.setBounds(340,35,65,30);

		//line 2: stack
		//75 pixels down
		lblS0=new Label();
		lblS0.setBackground(Color.LIGHT_GRAY);
		lblS0.setBounds(50,75,120,20);
		lblS0.setText("S0");
		lblS1=new Label();
		lblS1.setBackground(Color.LIGHT_GRAY);
		lblS1.setBounds(200,75,120,20);
		lblS1.setText("S1");
		//we could also add bottom of stack here

		//line 3: output
		//110 pixels down
		lblOut=new Label();
		lblOut.setBackground(Color.LIGHT_GRAY);
		lblOut.setBounds(50,110,30,20);
		lblOut.setText("OUT");
		lblMsg=new Label();
		lblMsg.setBackground(Color.LIGHT_GRAY);
		lblMsg.setBounds(100,110,200,20);

		//line 4: store/load neg
		//145 Pixels down
		bStore = new Button("STORE");
		bStore.setBounds(50,145,50,40);
		bLoad = new Button("LOAD");
		bLoad.setBounds(110,145,50,40);
		bSqrt = new Button("SQRT");
		//bNeg = new Button("NEG");
		bSqrt.setBounds(170,145,40,40);

		//line 5
		//200 pixels down
		bA = new Button("A");
		bA.setBounds(50,200,40,40);
		bB = new Button("B");
		bB.setBounds(100,200,40,40);
		bC = new Button("C");
		bC.setBounds(150,200,40,40);
		//bD needs added too
		bOpen = new Button("(");
		bOpen.setBounds(200,200,40,40);
		bClose = new Button(")");
		bClose.setBounds(250,200,40,40);

		//line 6: 7,8,9,FDIV
		//260 pixels down
		b7=new Button("7");
  		b7.setBounds(50,260,50,50);
		b8=new Button("8");
  		b8.setBounds(120,260,50,50);
		b9=new Button("9");
  		b9.setBounds(190,260,50,50);
  		bfdiv=new Button(DIVISION_SIGN);
  		bfdiv.setBounds(260,260,50,50);

		//line 7: 4,5,6, *
		//320 pixels down
		b4=new Button("4");
  		b4.setBounds(50,320,50,50);
		b5=new Button("5");
  		b5.setBounds(120,320,50,50);
		b6=new Button("6");
  		b6.setBounds(190,320,50,50);
		bmul=new Button("*");
  		bmul.setBounds(260,320,50,50);

		//line 8: 1,2,3 -
		//380 pixels down
		b1=new Button("1");
  		b1.setBounds(50,380,50,50);
		b2=new Button("2");
  		b2.setBounds(120,380,50,50);
		b3=new Button("3");
		b3.setBounds(190,380,50,50);
		bsub=new Button("-");
  		bsub.setBounds(260,380,50,50);

		//line 9: +/- 0 .  +
		//440 pixels down
		bsign=new Button("+/-");
  		bsign.setBounds(50,440,50,50);
		b0=new Button("0");
  		b0.setBounds(120,440,50,50);
		bpt=new Button(".");
  		bpt.setBounds(190,440,50,50);
  		badd=new Button("+");
		badd.setBounds(260,440,50,50);

		//line 10:
		bclr=new Button("CLR");
  		bclr.setBounds(50,500,50,40);
		bback=new Button("BACK");
 		bback.setBounds(120,500,50,40);
 		bspace=new Button("SPACE");
 		bspace.setBounds(190,500,60,40);

		//A,B,C variables
		lblA=new Label();
		lblA.setBackground(Color.LIGHT_GRAY);
		lblA.setBounds(350,200,120,30);
		lblA.setText("A");
		lblB=new Label();
		lblB.setBackground(Color.LIGHT_GRAY);
		lblB.setBounds(350,260,120,30);
		lblB.setText("B");
		lblC=new Label();
		lblC.setBackground(Color.LIGHT_GRAY);
		lblC.setBounds(350,320,120,30);
		lblC.setText("C");

		//add action listeners
		bEnter.addActionListener(this);
		bRun.addActionListener(this);
		bStore.addActionListener(this);
		bLoad.addActionListener(this);
		bSqrt.addActionListener(this);
		bA.addActionListener(this);
		bB.addActionListener(this);
		bC.addActionListener(this);
		//bD
		bOpen.addActionListener(this);
		bClose.addActionListener(this);

		b1.addActionListener(this);
		b2.addActionListener(this);
		b3.addActionListener(this);
		b4.addActionListener(this);
		b5.addActionListener(this);
		b6.addActionListener(this);
		b7.addActionListener(this);
		b8.addActionListener(this);
		b9.addActionListener(this);
		b0.addActionListener(this);

		badd.addActionListener(this);
		bsub.addActionListener(this);
		bmul.addActionListener(this);
		bfdiv.addActionListener(this);

		bpt.addActionListener(this);
		bsign.addActionListener(this);
		bclr.addActionListener(this);
		bback.addActionListener(this);
		bspace.addActionListener(this);

		//ADDING TO FRAME
		add(lblIn);  add(txtInput); add(bEnter); add(bRun);
		add(lblS0); add(lblS1);
		add(lblOut); add(lblMsg);
		add(bStore); add(bLoad); add(bSqrt); //add(bNeg);
		add(bA); add(bB); add(bC); add(bOpen); add(bClose);	//add(bD)
		add(b1); add(b2); add(b3); add(b4); add(b5);add(b6); add(b7); add(b8);add(b9);add(b0);
		add(badd); add(bsub); add(bmul); add(bfdiv);
		add(bsign); add(bpt);
		add(bclr); add(bback); add(bspace);
		add(lblA); add(lblB); add(lblC);

		setSize(550,600);
		setLayout(null);
		setVisible(true);
	}

	public void actionPerformed(ActionEvent e){
		String z;

		if (e.getSource()==b0){
  			z=txtInput.getText();
  			txtInput.setText(z+"0");
		} else
		if (e.getSource()==b1){
  			z=txtInput.getText();
  			txtInput.setText(z+"1");
		} else
		if (e.getSource()==b2){
  			z=txtInput.getText();
  			txtInput.setText(z+"2");
		} else
		if (e.getSource()==b3){
  			z=txtInput.getText();
  			txtInput.setText(z+"3");
		} else
		if (e.getSource()==b4){
  			z=txtInput.getText();
  			txtInput.setText(z+"4");
		} else
		if (e.getSource()==b5){
  			z=txtInput.getText();
  			txtInput.setText(z+"5");
		} else
		if (e.getSource()==b6){
  			z=txtInput.getText();
  			txtInput.setText(z+"6");
		} else
		if (e.getSource()==b7){
  			z=txtInput.getText();
  			txtInput.setText(z+"7");
		} else
		if (e.getSource()==b8){
  			z=txtInput.getText();
  			txtInput.setText(z+"8");
		} else
		if (e.getSource()==b9){
  			z=txtInput.getText();
  			txtInput.setText(z+"9");
		} else
		if(e.getSource()==bback){  // FOR  BACKSPACE
			z=txtInput.getText();
			if (z.length()>0) {
				try {
					z=z.substring(0, z.length()-1);
					txtInput.setText(z);
				} catch (StringIndexOutOfBoundsException x) {
					lblMsg.setText("BACKSPACE ERROR");
				}
			}
		} else
		if (e.getSource()==bspace) {
  			z=txtInput.getText();
  			txtInput.setText(z+" ");
		} else
		if(e.getSource()==bsign){ //to change sign
			z=txtInput.getText();
		  	if (z.startsWith("-")) {
				z=z.substring(1, z.length());
			  	txtInput.setText(z);
		  	} else {
		  		txtInput.setText("-"+z);
			}
		} else
		if(e.getSource()==bpt){ //FOR decimal point
		  z=txtInput.getText();
		  txtInput.setText(z+".");
		} else
		if(e.getSource()==badd){
			z=txtInput.getText();
  			txtInput.setText(z+" + ");
		} else
		if(e.getSource()==bsub){
			z=txtInput.getText();
  			txtInput.setText(z+" - ");
		} else
		if(e.getSource()==bmul){
			z=txtInput.getText();
  			txtInput.setText(z+" * ");
		} else
		if(e.getSource()==bfdiv){
			z=txtInput.getText();
  			txtInput.setText(z+" / ");
		} else
		if(e.getSource()==bclr){
			p.clear();
			refresh();
		} else
		if (e.getSource()==bStore) {
			z=txtInput.getText();
			txtInput.setText(z+" STORE_");
		} else
		if (e.getSource()==bLoad) {
			z=txtInput.getText();
			txtInput.setText(z+" LOAD_");
		} else
		if (e.getSource()==bSqrt) {
			z=txtInput.getText();
			txtInput.setText(z+" SQRT ");
		} else
		if (e.getSource()==bA) {
			z=txtInput.getText();
			txtInput.setText(z+"A");
		} else
		if (e.getSource()==bB) {
			z=txtInput.getText();
			txtInput.setText(z+"B");
		} else
		if (e.getSource()==bC) {
			z=txtInput.getText();
			txtInput.setText(z+"C");
		} else
		//bD
		if (e.getSource()==bOpen) {
			z=txtInput.getText();
			txtInput.setText(z+"(");
		} else
		if (e.getSource()==bClose) {
			z=txtInput.getText();
			txtInput.setText(z+")");
		} else
		if (e.getSource()==bEnter) {
			z=txtInput.getText();
			p.INPUT=z;
			try {
				p.compile();
			} catch (Exception x) {
				x.printStackTrace();
				lblMsg.setText(x.getMessage());
			}
			p.dumpOutput();
			//p.execute();
			refresh();
			//also display result in msg
			//int result = p.readS0();
			//float f = p.int24BitsToFloat(result);
			//lblMsg.setText(String.valueOf(f));
		} else
		if (e.getSource()==bRun) {
			p.execute();
			int result = p.readS0();
			float f = p.int24BitsToFloat(result);
			lblMsg.setText(String.valueOf(f));
		}
	}

	public void refresh() {
		txtInput.setText("");
		lblMsg.setText("");
		lblS0.setText(String.valueOf(p.readS0()));
		lblS1.setText(String.valueOf(p.readS1()));
		lblA.setText(String.valueOf(p.readA()));
		lblB.setText(String.valueOf(p.readB()));
		lblC.setText(String.valueOf(p.readC()));
	}

	public static void main(String[] args) {
		new L7ACalculator();
	}
}

