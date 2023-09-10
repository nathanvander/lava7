L7A Calculator
==============

This is based on my earlier L5D Calculator code.  I have put more effort into the README file there so you may want to read that as well.

You can type formulas into the IN box or use buttons.  When the ENTER button is pushed, the input is compiled into the internal machine language.
When you push RUN, it is executed, and the OUT label shows the result.  This lets you enter multiple lines before they are run.

Features
---------------------
This uses 24-bit floating point internally. 

This uses the Railroad Shuntyard Algorithm to compile.  This is pretty smart, so "SQRT(4)", "SQRT 4", and "4 SQRT" all produce the same result.

Known oddities and problems: 
----------------------------
Division By Zero doesn't show an error but it causes this to freeze up.

Make sure to clear the memory with CLR between runs.

The 3 gray labels to the right are supposed to display the contents of the A,B and C variables but they don't.
