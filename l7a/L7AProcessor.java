package lava7.l7a;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.io.IOException;
import java.util.Stack;

/**
* The L7AProcessor is the motherboard of the computer.  It needs a wrapper program, either
* on the command-line, or a windows program, to interact with it.
*
* This is based on my earlier L5D Processor.  The main difference is that the L5D uses
* 48-bit fixed-point numbers (which use 64-bit under the hood) and this uses 24-bit floating numbers
* (which use 32-bit under the hood).  This doesn't have as much range or accuracy, but I think
* 24-bit is a better size for what I want to do.
*
* Important note: when a number is a constant in memory, it is prefixed with 0x0100_0000 to mark it
* as a number.  When a number is on the stack or in local storage, it doesn't have the prefix.
*
* The way this works is you enter your formula into the INPUT field, and compile it into machine code
* which is stored in memory. Then you execute it. The answer will be in S0, the top of the stack.
*/
public class L7AProcessor {
	public final static String MODEL="L7A";
	public L7AProcessor() {}
	//=================================
	//Registers.  I put these at the top so they are visible

	public String INPUT;

	//to add to the stack, write to cell at the stack_pointer, then
	//	increment the stack pointer.
	//to remove something from the stack, decrement the stack pointer
	//	then zero out the value
	private int[] stack_space = new int[4];
	private IntStack stack = new IntStack(stack_space);
	//read top of stack
	public int readS0() {return stack.peek();}
	public int readS1() {return stack.deep();}

	//there are 4 local variables, A..D
	private int local_A;
	private int local_B;
	private int local_C;
	private int local_D;
	public int readA() {return local_A;}
	public int readB() {return local_B;}
	public int readC() {return local_C;}
	public int readD() {return local_D;}

	private int IP;		//instruction pointer, always 0 unless the program is running

	//================================
	//my own implementation of a stack
	public static class IntStack {
		int[] mem;
		//ptr holds the address of the next entry to be added, also it contains the size
		int ptr=0;

		//* pass in the backing array
		public IntStack(int[] m) {
			mem=m;
		}

		public void push(int v) {mem[ptr++]=v; }
		public void add(int v) {push(v);}

		public int size() {return ptr;}
		public int capacity() {return mem.length;}

		public void clear() {
			for (int i=0;i<ptr;i++) {
				mem[i]=0;
			}
			ptr=0;
		}

		public int peek() {
			if (ptr>0) {return mem[ptr-1];}
			else {
				//this is illegal array access, but just return 0
				return 0;
			}
		}

		public int pop() {
			//this could be combined into one line
			--ptr;
			return mem[ptr];
		}

		//swap top and 2nd from top
		public void swap() {
			if (ptr > 1) {
				int top = pop();
				int dos = pop();
				push(top);
				push(dos);
			}
		}

		public void dup() {
			push(peek());
		}

		//read second of stack without removing it
		public int deep() {
			if (ptr > 1) {
				return mem[ptr-2];
			} else {
				return 0;
			}
		}
	}

	//=================================
	//Memory
	//This is used to temporarily hold the compiled program
	//we may need to expand this
	private int[] memory = new int[64];
	//mem_stack is used when compiling to add to memory
	private IntStack mem_stack = new IntStack(memory);

	//=================================
	/**
	* Op CLR: Clear everything
	*/
	public void clear() {
		INPUT="";
		stack.clear();
		local_A=0;
		local_B=0;
		local_C=0;
		local_D=0;
		IP=0;
		//memory too
		mem_stack.clear();
	}
	//==================================
	//now to define tokens in our language.
	//Everything is an int
	//JVM opcodes start with 00
	//floating point constants start with 01
	//ascii characters start with 03
	//custom opcodes start with FF, subject to change

	//these are based on JVM codes
	//NOP and HALT have pretty much the same meaning
	public final static int NOP		= 0x0000_0000;
	public final static int NULL	= 0x0001_0000;
	public final static int POP		= 0x0057_0000;
	public final static int DUP		= 0x0059_0000;
	public final static int SWAP	= 0x005f_0000;

	//constants, these start with 0x01
	public final static int FK1 	= 0x013E_0000;  //constant 1.0F
	public final static int FK2 	= 0x0140_0000;	//constant 2.0F
	public final static int FKM1 	= 0x01BE_0000;	//constant -1.0F
	//these are not needed, I am just illustrating the range
	public final static int FKSMAX 	= 0x015B_FFF8;	//constant 32767.0F
	public final static int FKSMIN 	= 0x01DC_0000;	//constant -32768.0F

	public final static int FLOAD_0		= 0x0022_0000;	//	-> value	load a float value from local variable 0
	public final static int FLOAD_1		= 0x0023_0000;	//	-> value	load a float value from local variable 1
	public final static int FLOAD_2		= 0x0024_0000;	//	-> value	load a float value from local variable 2
	public final static int FLOAD_3		= 0x0025_0000;
	public final static int FSTORE_0	= 0x0043_0000;	//	value ->	store a float value into local variable 0
	public final static int FSTORE_1	= 0x0044_0000;	//	value ->	store a float value into local variable 1
	public final static int FSTORE_2	= 0x0045_0000;	//	value ->	store a float value into local variable 2
	public final static int FSTORE_3	= 0x0046_0000;
	public final static int FADD		= 0x0062_0000;
	public final static int FSUB		= 0x0066_0000;
	public final static int FMUL		= 0x006a_0000;
	public final static int FDIV		= 0x006e_0000;
	public final static int FNEG		= 0x0076_0000;	//negate
	public final static int FRET		= 0x00AE_0000;	//return a float - not needed yet

	//non standard opcodes, subject to change
	public final static int CLR			= 0xFF01_0000;
	public final static int SQRT		= 0xFF02_0000;
	public final static int HALT		= 0xFF03_0000;

	//ascii characters
	public final static int OPEN_PAREN 	= 0x0300_0028;	// "("
	public final static int SEMICOLON	= 0x0300_003B;	// ";"
	public final static int EQ 			= 0x0300_003D;	// "="
	public final static int CARAT 		= 0x0300_005E;	// "^"  used for power

	//===================================================
	public static int floatToInt24Bits(float fval) {
		if (fval == 0.0f) {
			return 0;
		}

		int sign = 0;
		int exp = 0; 	//this is 6 bits (0..5), so from -32 to 31
		int mant = 0;	//this is 18 bits, but the leading bit is discarded

		//first get the sign
		if (fval < 0.0f) {
			sign = 1;
			fval = 0.0f - fval;
		}

		//normalize the number.
		//We want the mantissa between 1 and 2
		while (fval >= 2.0f) {
			exp += 1;
			fval = fval / 2.0f;
		}
		while (fval < 1.0f) {
			exp -=1;
			fval = fval * 2.0f;
		}

		//the number should now be normalized
		mant = (int)( (fval - 1.0f) * 131072); // 2^17

		//assemble it
		int sign2 = sign * 8388608;	// 2^24 / 2
		//add 31 to bias the exponent
		int exp2 = (exp + 31) * 131072;
		return sign2 + exp2 + mant;
	}

	//see Float.intBitsToFloat()
	public static float int24BitsToFloat(int b) {
		if (b==0) {
			return 0.0f;
		}

		int sign = 0;	//sign is either 0 for positive or 1 for negative
		if (b > 8388607) {
			sign = 1;
			b = b - 8388608;	// 2^24 / 2
		}

		//get the biased exponent
		int exp = b / 131072;
		exp = exp - 31;	//remove bias

		//get the mantissa
		int mant = b % 131072;	//remainder after extracting the exponent
		final float divisor = 131072.0F;	//same as 0x800000
		float fval = ((float)mant / divisor) + 1.0f;

		//recreate number
		float n = 0.0f;
		if (exp == 0) {
			n = fval;
		} else {
			float pow = (float)Math.pow(2, exp);
			n = pow * fval;
		}

		//fix sign
		if (sign == 0) {
			return n;
		} else {
			return 0.f - n;
		}
	}

	public static boolean isFloatConstant(int b) {
		int kflag = b & 0x0100_0000;
		return (kflag > 0);
	}

	//convert a constant float to floating point
	//if this is not a constant float then return 0.0
	public static float k2f(int b) {
		if ( !isFloatConstant(b) ) {
			System.out.println("WARNING in k2f "+b+" is not a floating point constant");
			return 0.0f;
		} else {
			int fbits = b - 0x0100_0000;
			return int24BitsToFloat(fbits);
		}
	}

	//convert from constant to int24bits
	public static int k2i24(int x) {
		if ( !isFloatConstant(x) ) {
			System.out.println("WARNING in k2i24 "+x+" is not a floating point constant");
			return 0;
		} else {
			return x - 0x0100_0000;
		}
	}


	//=================================================
	/**
	* Compile the "source code", actually just a formula, into machine language.
	*/
	public void compile() {
		Shunt railyard = new Shunt(mem_stack,INPUT);
		railyard.run();
	}


	//==============================
	//this could be in a separate file, but let's keep it all together
	//create a new Shunt object each time you compile
	//this uses the memory from the enclosing class
	//see https://en.wikipedia.org/wiki/Shunting_yard_algorithm
	//
	//updated so this can compile multiple lines
	public static class Shunt implements Runnable {
		StreamTokenizer toker;
		IntStack operatorStack;
		IntStack outputQueue;

		public Shunt(IntStack mstack,String in) {
			toker = new StreamTokenizer(new StringReader(in));
			toker.eolIsSignificant(true);
			toker.wordChars(95,95);	//make underscore part of a word
			toker.ordinaryChar(47); //make forward slash an ordinary char
			operatorStack = new IntStack(new int[10]);
			//we need to append to memory, not rewrite it
			//outputQueue=new IntStack(mem);
			outputQueue = mstack;
		}

		public void run() {
			int tok = 0;
			try {
				tok=toker.nextToken();
			} catch (IOException x) {
				x.printStackTrace();
			}
			while (tok!= StreamTokenizer.TT_EOF) {
				//System.out.println("DEBUG: tok = "+tok);
				switch (tok) {
					case StreamTokenizer.TT_NUMBER:
						//make it into float24
						int fbits = floatToInt24Bits((float)toker.nval);
						int k = 0x0100_0000 + fbits;

						//add it to memory
						System.out.println("DEBUG: adding "+ toker.nval +" to ouput as "+ k);
						outputQueue.add(k);
						break;
					case StreamTokenizer.TT_WORD:
						String word = toker.sval;
						System.out.println("DEBUG: word = "+word);
						if (word.equals("A") || word.equals("LOAD_A")) {
							//this means to retrieve the value of A
							outputQueue.add(FLOAD_0);
						} else if (word.equals("B") || word.equals("LOAD_B")) {
							outputQueue.add(FLOAD_1);
						} else if (word.equals("C") || word.equals("LOAD_C")) {
							outputQueue.add(FLOAD_2);
						} else if (word.equals("D") || word.equals("LOAD_D")) {
							outputQueue.add(FLOAD_3);
						} else if (word.equals("STORE_A")) {
							outputQueue.add(FSTORE_0);
						} else if (word.equals("STORE_B")) {
							outputQueue.add(FSTORE_1);
						} else if (word.equals("STORE_C")) {
							outputQueue.add(FSTORE_2);
						} else if (word.equals("STORE_D")) {
							outputQueue.add(FSTORE_3);
						} else if (word.equals("SQRT")) {
							operatorStack.push(SQRT);
						} else {
							System.out.println("WARNING: unrecognized word "+word);
						}
						break;
					case 40:	// (
						operatorStack.push(OPEN_PAREN); break;
					case 41: 	// )
						doCloseParen(); break;
					case 42:	// *
						//go ahead and add the opcode
						operatorStack.push(FMUL); break;
					case 43:	// +
						doPlusOrMinus(tok); break;
					case 45:	// -
						doPlusOrMinus(tok); break;
					case 47:	// /
						operatorStack.push(FDIV); break;
					default:
						System.out.println("WARNING: unrecognized character "+tok+"("+(char)tok+")");
				} //end switch
				try {
					tok = toker.nextToken();
				} catch (IOException x) {
					x.printStackTrace();
				}
			} //end while
			//finally
			//When all the tokens have been read:
			//While there are still operator tokens in the stack:
			//Pop the operator on the top of the stack, and append it to the output.
			while (operatorStack.size()>0) {
				outputQueue.add(operatorStack.pop());
			}
			//now add HALT instruction
			//outputQueue.add(HALT);
		}	//end run

		/**
		* While there is an operator B of higher or equal precidence than A at the top of the stack,
		* pop B off the stack and append it to the output.
		* For this purpose, we only use 2 levels of precedence
		*/
		public boolean higher() {
			if (operatorStack.size()==0) {
				return false;
			} else {
				int e = operatorStack.peek();
				if ( (e==FMUL) || (e==FDIV)) {
					return true;
				} else {
					return false;
				}
			}
		}

		public void doPlusOrMinus(int op) {
			while (higher()) {
				outputQueue.add(operatorStack.pop());
			};
			if (op==43) {	// +
				operatorStack.push(FADD);
			} else if (op==45) {	// -
				operatorStack.push(FSUB);
			}
		}

		public void doCloseParen() {
			//If the token is a closing bracket:
			//Pop operators off the stack and append them to the output, until the operator at the top of the stack is a opening bracket.
			//Pop the opening bracket off the stack.
			while (operatorStack.size()>0) {
				int e = operatorStack.peek();
				if (e==OPEN_PAREN) {
					operatorStack.pop();
					return;
				} else {
					outputQueue.add(operatorStack.pop());
				}
			}
		}
	}	//close class Shunt

	//for debugging
	public void dumpOutput() {
		System.out.println("dump output");
		System.out.println("===========");
		for (int i=0;i<memory.length;i++) {
			int instruction = memory[i];
			if (instruction == HALT || instruction == 0) {
				System.out.println(i+": "+instruction + " (HALT)");
				break;
			} else {
				System.out.println(i+": "+instruction);
			}
		}
		System.out.println("===========");
	}

	//============================================
	//run the program, which is in queue
	boolean running = false;

	/**
	* execute the program one instruction at a time
	*/
	public void execute() {
		//add HALT instruction
		mem_stack.add(HALT);
		dumpOutput();

		System.out.println("execute");
		IP=-1;
		running = true;
		while (running) {
			//fetch the next byte
			int op = memory[++IP];
			System.out.println(IP+": "+op);
			switch_op(op);
		}
	}

	//this could be a switch
	private void switch_op(int op) {
		int a = 0;
		int b = 0;
		int c = 0;
		float fa = 0.0f;
		float fb = 0.0f;
		float fc = 0.0f;
		if (op==HALT) { running=false; return;}
		if (op==CLR) { clear(); return; }

		if (op==FADD) {
					b = stack.pop();
					//System.out.println("switch_op: popping (raw)"+b+" from stack");
					fb = int24BitsToFloat(b);
					//System.out.println("switch_op: popping "+fb+" from stack");
					a = stack.pop();
					fa = int24BitsToFloat(a);
					//System.out.println("switch_op: popping (raw)"+a+" from stack");
					//System.out.println("switch_op: popping "+fa+" from stack");
					fc = fa + fb;
					//System.out.println("switch_op: pushing "+fc+" to stack");
					c = floatToInt24Bits(fc);
					stack.push( c );
					//System.out.println("switch_op: pushing (raw) "+c+" to stack");
					return;
		}
		if (op==FSUB) {
					b = stack.pop();
					fb = int24BitsToFloat(b);
					a = stack.pop();
					fa = int24BitsToFloat(a);
					fc = fa - fb;
					c = floatToInt24Bits(fc);
					stack.push( c );
					return;
		}
		if (op==FMUL) {
					b = stack.pop();
					fb = int24BitsToFloat(b);
					a = stack.pop();
					fa = int24BitsToFloat(a);
					fc = fa * fb;
					c = floatToInt24Bits(fc);
					stack.push( c );
					return;
		}
		if (op==FDIV) {
					b = stack.pop();
					fb = int24BitsToFloat(b);
					a = stack.pop();
					fa = int24BitsToFloat(a);
					fc = fa / fb;
					c = floatToInt24Bits(fc);
					stack.push( c );
					return;
		}

		if (op==SQRT) {
					a = stack.pop();
					fa = int24BitsToFloat(a);
					fc = (float)Math.sqrt((double)fa);
					c = floatToInt24Bits(fc);
					stack.push( c);
					return;
		}
		if (op==FLOAD_0) { stack.push(local_A); return;}
		if (op==FLOAD_1) { stack.push(local_B); return;}
		if (op==FLOAD_2) { stack.push(local_C); return;}
		if (op==FLOAD_3) { stack.push(local_D); return;}
		if (op==FSTORE_0) { local_A=stack.pop(); return;}
		if (op==FSTORE_1) { local_B=stack.pop(); return;}
		if (op==FSTORE_2) { local_C=stack.pop(); return;}
		if (op==FSTORE_3) { local_D=stack.pop(); return;}
		if (isFloatConstant(op)) {
			c = k2i24(op);
			stack.push(c);
			System.out.println("switch_op: pushing "+c+" on to stack");
			return;
		}
		System.out.println("WARNING: unrecognized instruction "+op);
	}
}