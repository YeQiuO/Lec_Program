import dataType.*;
import exception.IncompatibleTypeError;
import exception.OperatorError;

import java.io.*;
import java.util.ArrayList;
import java.util.regex.Pattern;

/**
 * @author momoshenchi
 * @date 2021-06-12
 */
public class Machine
{
    private final static int STACKSIZE = 1000;

//            args = new String[1];
//        args[0] = "D:\\Yuby\\Cuby\\testing\\ex(float).out";
//       args[1] = "Hello";
//       args[2] = "1.2";
//       args[3] = "100";
//       args[4] = "123Hel";

    public static void main(String[] args) throws FileNotFoundException, IOException, OperatorError, IncompatibleTypeError
    {
        if (args.length == 0)
        {
            System.out.println("Usage: java Machine <programfile> <arg1> ...\n");
        }
        else
        {
            execute(args, false);
        }

    }


    static void execute(String[] args, boolean trace) throws FileNotFoundException, IOException, OperatorError, IncompatibleTypeError
    {
        ArrayList<Integer> program = readfile(args[0]);

        BaseType[] stack = new BaseType[STACKSIZE];

        BaseType[] inputArgs = new BaseType[args.length - 1];

        for (int i = 1; i < args.length; i++)
        {
            if (Pattern.compile("(?i)[a-z]").matcher(args[i]).find())
            {
                char[] input = args[i].toCharArray();
                CharType[] array = new CharType[input.length];
                for (int j = 0; j < input.length; ++j)
                {
                    array[j] = new CharType(input[j]);
                }
                inputArgs[i - 1] = new ArrayType(array);
            }
            else if (args[i].contains("."))
            {
                inputArgs[i - 1] = new FloatType(Float.parseFloat(args[i]));
            }
            else
            {
                inputArgs[i - 1] = new IntType(Integer.parseInt(args[i]));
            }
        }

        long startTime = System.currentTimeMillis();
        execCode(program, stack, inputArgs, trace);
        long runtime = System.currentTimeMillis() - startTime;
        System.err.println("\nRan " + runtime / 1000.0 + " seconds");
    }

    private static int execCode(ArrayList<Integer> program, BaseType[] stack, BaseType[] inputArgs, boolean trace) throws IncompatibleTypeError, OperatorError
    {
        int bp = -999;
        int sp = -1;
        int pc = 0;
        int hr = -1;
        for (; ; )
        {
            if (trace)
            {
                printSpPc(stack, bp, sp, program, pc);
            }
            switch (program.get(pc++))
            {
                case Instruction.CSTI:
                    stack[sp + 1] = new IntType(program.get(pc++));
                    sp++;
                    break;
                case Instruction.CSTF:
                    stack[sp + 1] = new FloatType(Float.intBitsToFloat(program.get(pc++)));
                    sp++;
                    break;
                case Instruction.CSTD:
                    int a = program.get(pc++);
                    int b = program.get(pc++);
                    String c = Integer.toBinaryString(a) + binaryFormat(Integer.toBinaryString(b));
                    Long longTemp = Long.valueOf(c, 2);
                    stack[sp + 1] = new DoubleType(Double.longBitsToDouble(longTemp));
                    sp++;
                    break;
                case Instruction.CSTL:
                    int longa = program.get(pc++);
                    int longb = program.get(pc++);
                    String longc = Integer.toBinaryString(longa) + binaryFormat(Integer.toBinaryString(longb));
                    Long aLong = Long.valueOf(longc, 2);
                    stack[sp + 1] = new LongType(aLong);
                    sp++;
                    break;
                case Instruction.CSTC:
                    stack[sp + 1] = new CharType((char) (program.get(pc++).intValue()));
                    sp++;
                    break;
                case Instruction.ADD:
                {
                    stack[sp - 1] = binaryOperator(stack[sp - 1], stack[sp], "+");
                    sp--;
                    break;
                }
                case Instruction.SUB:
                {
                    stack[sp - 1] = binaryOperator(stack[sp - 1], stack[sp], "-");
                    sp--;
                    break;
                }
                case Instruction.MUL:
                {
                    stack[sp - 1] = binaryOperator(stack[sp - 1], stack[sp], "*");
                    sp--;
                    break;
                }
                case Instruction.DIV:
                    if (((IntType) stack[sp]).getValue() == 0)
                    {
                        System.out.println("hr:" + hr + " exception:" + 1);
                        while (hr != -1 && ((IntType) stack[hr]).getValue() != 1)
                        {
                            hr = ((IntType) stack[hr + 2]).getValue();
                            System.out.println("hr:" + hr + " exception:" + new IntType(program.get(pc)).getValue());
                        }

                        if (hr != -1)
                        {
                            sp = hr - 1;
                            pc = ((IntType) stack[hr + 1]).getValue();
                            hr = ((IntType) stack[hr + 2]).getValue();
                        }
                        else
                        {
                            System.out.print(hr + "not find exception");
                            return sp;
                        }
                    }
                    else
                    {
                        stack[sp - 1] = binaryOperator(stack[sp - 1], stack[sp], "/");
                        sp--;
                    }

                    break;
                case Instruction.MOD:
                    stack[sp - 1] = binaryOperator(stack[sp - 1], stack[sp], "%");
                    sp--;
                    break;
                case Instruction.EQ:
                    stack[sp - 1] = binaryOperator(stack[sp - 1], stack[sp], "==");
                    sp--;
                    break;
                case Instruction.LT:
                    stack[sp - 1] = binaryOperator(stack[sp - 1], stack[sp], "<");
                    sp--;
                    break;
                case Instruction.NOT:
                {
                    Object result = null;
                    if (stack[sp] instanceof FloatType)
                    {
                        result = ((FloatType) stack[sp]).getValue();
                    }
                    else if (stack[sp] instanceof IntType)
                    {
                        result = ((IntType) stack[sp]).getValue();
                    }
                    stack[sp] = (Float.compare(new Float(result.toString()), 0.0f) == 0 ? new IntType(1) : new IntType(0));
                    break;
                }
                case Instruction.DUP:
                    stack[sp + 1] = stack[sp];
                    sp++;
                    break;
                case Instruction.SWAP:
                {
                    BaseType tmp = stack[sp];
                    stack[sp] = stack[sp - 1];
                    stack[sp - 1] = tmp;
                    break;
                }
                case Instruction.LDI:
                    stack[sp] = stack[((IntType) stack[sp]).getValue()];
                    break;
                case Instruction.STI:
                    stack[((IntType) stack[sp - 1]).getValue()] = stack[sp];
                    stack[sp - 1] = stack[sp];
                    sp--;
                    break;
                case Instruction.GETBP:
                    stack[sp + 1] = new IntType(bp);
                    sp++;
                    break;
                case Instruction.GETSP:
                    stack[sp + 1] = new IntType(sp);
                    sp++;
                    break;
                case Instruction.INCSP:
                    sp = sp + program.get(pc++);
                    break;
                case Instruction.GOTO:
                    pc = program.get(pc);
                    break;
                case Instruction.IFZERO:
                {
                    Object result = null;
                    int index = sp--;
                    if (stack[index] instanceof IntType)
                    {
                        result = ((IntType) stack[index]).getValue();
                    }
                    else if (stack[index] instanceof FloatType)
                    {
                        result = ((FloatType) stack[index]).getValue();
                    }
                    pc = (Float.compare(new Float(result.toString()), 0.0f) == 0 ? program.get(pc) : pc + 1);
                    break;
                }
                case Instruction.IFNZRO:
                {
                    Object result = null;
                    int index = sp--;
                    if (stack[index] instanceof IntType)
                    {
                        result = ((IntType) stack[index]).getValue();
                    }
                    else if (stack[index] instanceof FloatType)
                    {
                        result = ((FloatType) stack[index]).getValue();
                    }
                    pc = (Float.compare(new Float(result.toString()), 0.0f) != 0 ? program.get(pc) : pc + 1);
                    break;
                }
                case Instruction.CALL:
                {
                    int argc = program.get(pc++);
                    for (int i = 0; i < argc; i++)
                    {
                        stack[sp - i + 2] = stack[sp - i];
                    }
                    stack[sp - argc + 1] = new IntType(pc + 1);
                    sp++;
                    stack[sp - argc + 1] = new IntType(bp);
                    sp++;
                    bp = sp + 1 - argc;
                    pc = program.get(pc);
                    break;
                }
                case Instruction.TCALL:
                {
                    int argc = program.get(pc++);
                    int pop = program.get(pc++);
                    for (int i = argc - 1; i >= 0; i--)
                    {
                        stack[sp - i - pop] = stack[sp - i];
                    }
                    sp = sp - pop;
                    pc = program.get(pc);
                }
                break;
                case Instruction.RET:
                {
                    BaseType res = stack[sp];
                    sp = sp - program.get(pc);
                    bp = ((IntType) stack[--sp]).getValue();
                    pc = ((IntType) stack[--sp]).getValue();
                    stack[sp] = res;
                }
                break;
                case Instruction.PRINTI:
                {
                    Object result;
                    if (stack[sp] instanceof IntType)
                    {
                        result = ((IntType) stack[sp]).getValue();
                    }
                    else if (stack[sp] instanceof FloatType)
                    {
                        result = ((FloatType) stack[sp]).getValue();
                    }
                    else if (stack[sp] instanceof DoubleType)
                    {
                        result = ((DoubleType) stack[sp]).getValue();
                    }
                    else if (stack[sp] instanceof CharType)
                    {
                        result = ((CharType) stack[sp]).getValue();
                    }
                    else if (stack[sp] instanceof LongType)
                    {
                        result = ((LongType) stack[sp]).getValue();
                    }
                    else
                    {
                        throw new RuntimeException("未知类型");
                    }
                    System.out.print(String.valueOf(result) + " ");
                    break;
                }
                case Instruction.PRINTC:
                    System.out.print((((CharType) stack[sp])).getValue());
                    break;
                case Instruction.LDARGS:
                    // Push commandline arguments
                    for (BaseType inputArg : inputArgs)
                    {
                        stack[++sp] = inputArg;
                    }
                    break;
                case Instruction.STOP:
                    return sp;
                // case Instruction.PUSHHR:
                // {
                //     stack[++sp] = new IntType(program.get(pc++));    //exn
                //     int tmp = sp;       //exn address
                //     sp++;
                //     stack[sp++] = new IntType(program.get(pc++));   //jump address
                //     stack[sp] = new IntType(hr);
                //     hr = tmp;
                //     break;
                // }
                // case Instruction.POPHR:
                //     hr = ((IntType) stack[sp--]).getValue();
                //     sp -= 2;
                //     break;
                // case Instruction.THROW:
                //     System.out.println("hr:" + hr + " exception:" + new IntType(program.get(pc)).getValue());

                //     while (hr != -1 && ((IntType) stack[hr]).getValue() != program.get(pc))
                //     {
                //         hr = ((IntType) stack[hr + 2]).getValue(); //find exn address
                //         System.out.println("hr:" + hr + " exception:" + new IntType(program.get(pc)).getValue());
                //     }

                //     if (hr != -1)
                //     { // Found a handler for exn
                //         sp = hr - 1;    // remove stack after hr
                //         pc = ((IntType) stack[hr + 1]).getValue();
                //         hr = ((IntType) stack[hr + 2]).getValue(); // with current handler being hr
                //     }
                //     else
                //     {
                //         System.out.print(hr + "not find exception");
                //         return sp;
                //     }
                //     break;

                default:
                    throw new RuntimeException("Illegal instruction " + program.get(pc - 1)
                            + " at address " + (pc - 1));
            }
        }
    }

    private static String binaryFormat(String str)
    {
        StringBuilder strBuilder = new StringBuilder(str);
        while (strBuilder.length() < 32)
        {
            strBuilder.insert(0,0);
        }
        str = strBuilder.toString();
        return str;
    }

    public static Number getValueFromType(BaseType baseType) throws IncompatibleTypeError
    {

        if (baseType instanceof FloatType)
        {
            return ((FloatType) baseType).getValue();
        }
        else if (baseType instanceof IntType)
        {
            return ((IntType) baseType).getValue();
        }
        else if (baseType instanceof DoubleType)
        {
            return ((DoubleType) baseType).getValue();
        }
        else if (baseType instanceof LongType)
        {
            return ((LongType) baseType).getValue();
        }
        else
        {
            throw new IncompatibleTypeError("IncompatibleTypeError: " + baseType + " type is not int or float");
        }

    }

/**
 *     TODO: 我去,这个屎山一样的代码应该怎么优化啊. 不能用包装类接收,只能一个一个判断转换为基本类型,就离谱
  */
    public static BaseType binaryOperator(BaseType lhs, BaseType rhs, String operator) throws IncompatibleTypeError, OperatorError
    {
        int flag = -1;
        BaseType result = null;

        //目前设定左右运算符类型一致,只检查左边
        //左右不一致代码冗长,不会处理
        if (lhs instanceof FloatType)
        {
            flag = Instruction.CSTF;
        }
        else if (lhs instanceof IntType)
        {
            flag = Instruction.CSTI;
        }
        else if (lhs instanceof DoubleType)
        {
            flag = Instruction.CSTD;
        }
        else if (lhs instanceof LongType)
        {
            flag = Instruction.CSTL;
        }
        else
        {
            throw new IncompatibleTypeError("IncompatibleTypeError: " + lhs + " type is not int or float");
        }
        switch (operator)
        {
            case "+":
            {
                if (flag == Instruction.CSTF)
                {
                    result = new FloatType(((FloatType) lhs).getValue() + ((FloatType) rhs).getValue());
                }
                else if (flag == Instruction.CSTD)
                {
                    result = new DoubleType(((DoubleType) lhs).getValue() + ((DoubleType) rhs).getValue());
                }
                else if (flag == Instruction.CSTI)
                {
                    result = new IntType(((IntType) lhs).getValue() + ((IntType) rhs).getValue());
                }
                else
                {
                    result = new LongType(((LongType) lhs).getValue() + ((LongType) rhs).getValue());
                }
            }
            break;
            case "-":
            {
                if (flag == Instruction.CSTF)
                {
                    result = new FloatType(((FloatType) lhs).getValue() - ((FloatType) rhs).getValue());
                }
                else if (flag == Instruction.CSTD)
                {
                    result = new DoubleType(((DoubleType) lhs).getValue() - ((DoubleType) rhs).getValue());
                }
                else if (flag == Instruction.CSTI)
                {
                    result = new IntType(((IntType) lhs).getValue() - ((IntType) rhs).getValue());
                }
                else
                {
                    result = new LongType(((LongType) lhs).getValue() - ((LongType) rhs).getValue());
                }
            }
            break;
            case "*":
            {
                if (flag == Instruction.CSTF)
                {
                    result = new FloatType(((FloatType) lhs).getValue() * ((FloatType) rhs).getValue());
                }
                else if (flag == Instruction.CSTD)
                {
                    result = new DoubleType(((DoubleType) lhs).getValue() * ((DoubleType) rhs).getValue());
                }
                else if (flag == Instruction.CSTI)
                {
                    result = new IntType(((IntType) lhs).getValue() * ((IntType) rhs).getValue());
                }
                else
                {
                    result = new LongType(((LongType) lhs).getValue() * ((LongType) rhs).getValue());
                }
            }
            break;
            case "/":
            {
                // if(flag==Instruction.CSTF){
                //     float ff=((FloatType)rhs).getValue();
                //     if(ff-0<0.0001)
                //     {
                //          throw new OperatorError("OpeatorError: Divisor can't not be zero");
                //     }
                // }
                // if(flag==Instruction.CSTD){
                //     double ff=((DoubleType)rhs).getValue();
                //     if(ff-0<0.0001)
                //     {
                //          throw new OperatorError("OpeatorError: Divisor can't not be zero");
                //     }
                // }
                // if(flag==Instruction.CSTF){
                //     long ff=((LongType)rhs).getValue();
                //     if(ff-0<0.0001)
                //     {
                //          throw new OperatorError("OpeatorError: Divisor can't not be zero");
                //     }
                // }
                if (flag == Instruction.CSTF)
                {
                    float ff=((FloatType)rhs).getValue();
                    if(ff-0<0.0001)
                    {
                         throw new OperatorError("OpeatorError: Divisor can't not be zero");
                    }
                    result = new FloatType(((FloatType) lhs).getValue() / ((FloatType) rhs).getValue());
                }
                else if (flag == Instruction.CSTD)
                {
                    double ff=((DoubleType)rhs).getValue();
                    if(ff-0<0.0001)
                    {
                         throw new OperatorError("OpeatorError: Divisor can't not be zero");
                    }
                    result = new DoubleType(((DoubleType) lhs).getValue() / ((DoubleType) rhs).getValue());
                }
                else if (flag == Instruction.CSTI)
                {
                    int ff=((IntType)rhs).getValue();
                    if(ff-0<0.0001)
                    {
                         throw new OperatorError("OpeatorError: Divisor can't not be zero");
                    }
                    result = new IntType(((IntType) lhs).getValue() / ((IntType) rhs).getValue());
                }
                else
                {
                    long ff=((LongType)rhs).getValue();
                    if(ff-0<0.0001)
                    {
                         throw new OperatorError("OpeatorError: Divisor can't not be zero");
                    }
                    result = new LongType(((LongType) lhs).getValue() / ((LongType) rhs).getValue());
                }
            }
            break;
            case "%":
            {
                if (flag != Instruction.CSTI)
                {
                    throw new OperatorError("OperatorError: Float can't mod");
                }
                else
                {
                    result = new IntType(((IntType) lhs).getValue() % ((IntType) rhs).getValue());
                }
            }
            break;
            case "==":
            {
                if (flag == Instruction.CSTF)
                {
                    result = booleanToIntType(((FloatType) lhs).getValue() == ((FloatType) rhs).getValue());
                }
                else if (flag == Instruction.CSTD)
                {
                    result = booleanToIntType(((DoubleType) lhs).getValue() == ((DoubleType) rhs).getValue());
                }
                else if (flag == Instruction.CSTI)
                {
                    result = booleanToIntType(((IntType) lhs).getValue() == ((IntType) rhs).getValue());
                }
                else
                {
                    result = booleanToIntType(((LongType) lhs).getValue() == ((LongType) rhs).getValue());
                }
            }
            break;
            case "<":
            {
                if (flag == Instruction.CSTF)
                {
                    result = booleanToIntType(((FloatType) lhs).getValue() < ((FloatType) rhs).getValue());
                }
                else if (flag == Instruction.CSTD)
                {
                    result = booleanToIntType(((DoubleType) lhs).getValue() < ((DoubleType) rhs).getValue());
                }
                else if (flag == Instruction.CSTI)
                {
                    result = booleanToIntType(((IntType) lhs).getValue() < ((IntType) rhs).getValue());
                }
                else
                {
                    result = booleanToIntType(((LongType) lhs).getValue() < ((LongType) rhs).getValue());
                }
            }
            break;
            case ">":
            {
                if (flag == Instruction.CSTF)
                {
                    result = booleanToIntType(((FloatType) lhs).getValue() > ((FloatType) rhs).getValue());
                }
                else if (flag == Instruction.CSTD)
                {
                    result = booleanToIntType(((DoubleType) lhs).getValue() > ((DoubleType) rhs).getValue());
                }
                else if (flag == Instruction.CSTI)
                {
                    result = booleanToIntType(((IntType) lhs).getValue() > ((IntType) rhs).getValue());
                }
                else
                {
                    result = booleanToIntType(((LongType) lhs).getValue() > ((LongType) rhs).getValue());
                }
            }
            break;
            default:
                break;
        }
        return result;
    }

    private static IntType booleanToIntType(boolean b)
    {
        if (b)
        {
            return new IntType(1);
        }
        else
        {
            return new IntType(0);
        }
    }

    private static String insName(ArrayList<Integer> program, int pc)
    {
        switch (program.get(pc))
        {
            case Instruction.CSTI:
                return "CSTI " + program.get(pc + 1);
            case Instruction.CSTF:
                return "CSTF " + program.get(pc + 1);
            case Instruction.CSTD:
                return "CSTD " + program.get(pc + 1) + program.get(pc + 2);
            case Instruction.CSTL:
                return "CSTL " + program.get(pc + 1) + program.get(pc + 2);
            case Instruction.CSTC:
                return "CSTC " + (char) (program.get(pc + 1).intValue());
            case Instruction.ADD:
                return "ADD";
            case Instruction.SUB:
                return "SUB";
            case Instruction.MUL:
                return "MUL";
            case Instruction.DIV:
                return "DIV";
            case Instruction.MOD:
                return "MOD";
            case Instruction.EQ:
                return "EQ";
            case Instruction.LT:
                return "LT";
            case Instruction.NOT:
                return "NOT";
            case Instruction.DUP:
                return "DUP";
            case Instruction.SWAP:
                return "SWAP";
            case Instruction.LDI:
                return "LDI";
            case Instruction.STI:
                return "STI";
            case Instruction.GETBP:
                return "GETBP";
            case Instruction.GETSP:
                return "GETSP";
            case Instruction.INCSP:
                return "INCSP " + program.get(pc + 1);
            case Instruction.GOTO:
                return "GOTO " + program.get(pc + 1);
            case Instruction.IFZERO:
                return "IFZERO " + program.get(pc + 1);
            case Instruction.IFNZRO:
                return "IFNZRO " + program.get(pc + 1);
            case Instruction.CALL:
                return "CALL " + program.get(pc + 1) + " " + program.get(pc + 2);
            case Instruction.TCALL:
                return "TCALL " + program.get(pc + 1) + " " + program.get(pc + 2) + " " + program.get(pc + 3);
            case Instruction.RET:
                return "RET " + program.get(pc + 1);
            case Instruction.PRINTI:
                return "PRINTI";
            case Instruction.PRINTC:
                return "PRINTC";
            case Instruction.LDARGS:
                return "LDARGS";
            case Instruction.STOP:
                return "STOP";
            // case Instruction.THROW:
            //     return "THROW" + program.get(pc + 1);
            // case Instruction.PUSHHR:
            //     return "PUSHHR" + " " + program.get(pc + 1) + " " + program.get(pc + 2);
            // case Instruction.POPHR:
            //     return "POPHR";
            default:
                return "<unknown>";
        }
    }


    private static void printSpPc(BaseType[] stack, int bp, int sp, ArrayList<Integer> program, int pc)
    {
        System.out.print("[ ");
        for (int i = 0; i <= sp; i++)
        {
            Object result = null;
            if (stack[i] instanceof IntType)
            {
                result = ((IntType) stack[i]).getValue();
            }
            else if (stack[i] instanceof FloatType)
            {
                result = ((FloatType) stack[i]).getValue();
            }
            else if (stack[i] instanceof DoubleType)
            {
                result = ((DoubleType) stack[i]).getValue();
            }
            else if (stack[i] instanceof CharType)
            {
                result = ((CharType) stack[i]).getValue();
            }
            else if (stack[i] instanceof LongType)
            {
                result = ((LongType) stack[i]).getValue();
            }
            System.out.print(String.valueOf(result) + " ");
        }
        System.out.print("]");
        System.out.println("{" + pc + ": " + insName(program, pc) + "}");
    }


    private static ArrayList<Integer> readfile(String filename) throws FileNotFoundException, IOException
    {
        ArrayList<Integer> program = new ArrayList<Integer>();
        Reader inp = new FileReader(filename);

        StreamTokenizer tStream = new StreamTokenizer(inp);
        tStream.parseNumbers();
        tStream.nextToken();
        while (tStream.ttype == StreamTokenizer.TT_NUMBER)
        {
            program.add((int) tStream.nval);
            tStream.nextToken();
        }

        inp.close();

        return program;
    }
}


class Machinetrace
{
    public static void main(String[] args)
            throws FileNotFoundException, IOException, OperatorError, IncompatibleTypeError
    {
        if (args.length == 0)
        {
            System.out.println("Usage: java Machinetrace <programfile> <arg1> ...\n");
        }
        else
        {
            Machine.execute(args, true);
        }
    }
}
