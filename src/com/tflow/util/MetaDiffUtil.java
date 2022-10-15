package com.tflow.util;

import org.mariuszgromada.math.mxparser.Expression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static com.tflow.util.MetaDiffUtil.Operator.*;

/**
 * Notice: This Util for PlayGround only, please don't make static function in here.
 */
public class MetaDiffUtil {

    /**
     * ordered by Priority, high ordinal() high priority
     */
    public enum Operator {
        MUL("*", "/", false, 4),
        DIV("/", "*", true, 3),
        ADD("+", "-", false, 2) /*ADD is highest priority*/,
        SUB("-", "+", true, 1),
        ;

        public String symbol;
        public String opposite;
        public boolean negative;
        public int weight;

        Operator(String symbol, String opposite, boolean negative, int weight) {
            this.symbol = symbol;
            this.opposite = opposite;
            this.negative = negative;
            this.weight = weight;
        }

        public static Operator parse(String symbol) {
            for (Operator operator : values()) {
                if (operator.symbol.equals(symbol)) {
                    return operator;
                }
            }
            return null;
        }

        @Override
        public String toString() {
            return name() + "( " + symbol + " )";
        }
    }

    public class Operand {
        public Operator operator;
        public double operand;

        public Operand(Operator operator, double operand) {
            this.operator = operator;
            this.operand = operand;
        }

        public boolean isSame(Operand o) {
            if (o == null) return false;
            return Double.compare(o.operand, operand) == 0 && operator == o.operator;
        }

        public boolean isSameOperator(Operand o) {
            if (o == null) return false;
            return operator == o.operator;
        }

        public boolean isSameOperand(Operand o) {
            if (o == null) return false;
            return Double.compare(o.operand, operand) == 0;
        }

        @Override
        public int hashCode() {
            return Objects.hash(operator, operand);
        }

        @Override
        public String toString() {
            return operator.name() + "(" + operand + ")";
        }
    }

    public class MetaDiff {
        public int current;
        public int next;
        public List<Operand> operandList;

        public MetaDiff() {
            operandList = new ArrayList<>();
        }

        @Override
        public String toString() {
            return "{" +
                    "current:" + current +
                    ", next:" + next +
                    ", diff:" + Arrays.toString(operandList.toArray()) +
                    '}';
        }
    }

    private Logger log;

    public MetaDiffUtil() {
        log = LoggerFactory.getLogger(getClass());
    }

    public MetaDiff newMetaDiff() {
        return new MetaDiff();
    }


    public List<MetaDiff> createMetaDiff(List<Integer> intArray) {
        List<MetaDiff> metaDiffList = new ArrayList<>();

        MetaDiff metaDiff;
        int operand = -1;
        for (int intValue : intArray) {
            if (operand < 0) {
                operand = intValue;
                continue;
            }

            metaDiff = getMetaDiff(operand, intValue);
            metaDiffList.add(metaDiff);

            operand = intValue;
        }

        return metaDiffList;
    }

    private MetaDiff getMetaDiff(int source, int target) {
        MetaDiff metaDiff = new MetaDiff();
        metaDiff.current = source;
        metaDiff.next = target;

        /*try each <operator> to find <operand> that fit <exp2>*/
        for (Operator operator : values()) {
            if (operator.negative)
                addOperandNegative(metaDiff.operandList, operator, source, target);
            else
                addOperand(metaDiff.operandList, operator, source, target);
        }

        return metaDiff;
    }

    private void addOperandNegative(List<Operand> operandList, Operator operator, int source, int target) {
        log.debug("addOperandNegative: oper:{}, src:{}, tar:{}", operator, source, target);
        /*Notice: find how to create target by source
         * exp1: target = source / <operand>
         * exp2: <operand> = source / target
         **/
        String finder;
        String checker;
        double operand;
        double targetDouble;
        boolean sourceMax = source >= target;

        /*try exp2*/
        finder = sourceMax ? (source + operator.symbol + target) : target + operator.symbol + source;
        operand = calculate(finder, "finder");

        /*try exp1*/
        checker = (sourceMax ? source : target) + operator.symbol + operand;
        targetDouble = calculate(checker, "checker");

        int intValue = Double.valueOf(targetDouble).intValue();
        if (target - intValue == 0) {
            Operand operandObject = new Operand(operator, operand);
            operandList.add(operandObject);
            log.debug("fit: ({},{}) => {}", source, target, operandObject);
        } else {
            log.debug("not-fit: ({},{}) => {}:{}", source, target, operator, operand);
        }
    }

    private void addOperand(List<Operand> operandList, Operator operator, int source, int target) {
        log.debug("addOperand: oper:{}, src:{}, tar:{}", operator, source, target);
        /*Notice: find how to create target by source
         * exp1: target = source <operator> <operand>
         * exp2: <operand> = target <opposite-operator> source
         **/

        /*try exp2*/
        String finder = target + operator.opposite + source;
        double operand = calculate(finder, "finder");

        /*try exp1*/
        String checker = source + operator.symbol + operand;
        double targetDouble = calculate(checker, "checker");

        int intValue = Double.valueOf(targetDouble).intValue();
        if (target - intValue == 0) {
            Operand operandObject = new Operand(operator, operand);
            operandList.add(operandObject);
            log.debug("fit: ({},{}) => {}", source, target, operandObject);
        } else {
            log.debug("not-fit: ({},{}) => {}:{}", source, target, operator, operand);
        }
    }

    private double calculate(String expressionString, String label) {
        Expression expression = new Expression(expressionString);
        double calculated = expression.calculate();
        log.debug("calculate:{}:'{}' = {}", label, expressionString, calculated);
        return calculated;
    }

    public double estimate(int currentValue, MetaDiff metaDiff) {
        //estimateByMaxWeight(currentValue,metaDiff);

        /*use current-operator as finder and use other-operators as checker for diff-value*/
        List<Double> nextList = new ArrayList<>();
        List<Double> diffList = new ArrayList<>();
        String expression;
        double nextValue;
        for (Operand operand : metaDiff.operandList) {
            expression = currentValue + operand.operator.symbol + operand.operand;
            nextValue = calculate(expression, "estimate:finder");
            nextList.add(nextValue);
            diffList.add(findDiff(currentValue, nextValue, diffList.size(), metaDiff.operandList));
        }

        /*find min diff*/
        int minIndex = 0;
        Double minDiff = Double.MAX_VALUE;
        Double aDouble;
        int size = diffList.size();
        for (int index = 0; index < size; index++) {
            aDouble = diffList.get(index);
            if (aDouble.compareTo(minDiff) < 0) {
                minIndex = index;
                minDiff = aDouble;
            }
        }

        return nextList.get(minIndex);
    }

    private Double findDiff(double currentValue, double nextValue, int skipIndex, List<Operand> operandList) {
        String expression;
        Operand operand;
        double diff = 0;
        int size = operandList.size();
        for (int index = 0; index < size; index++) {
            if (index == skipIndex) continue;

            operand = operandList.get(index);
            expression = nextValue + operand.operator.opposite + currentValue;
            diff += Math.abs(calculate(expression, "findDiff"));
        }
        return diff;
    }

    public double estimateByMaxWeight(int currentValue, MetaDiff metaDiff) {
        Operand operand = getMaxWeight(metaDiff.operandList);
        String expression = currentValue + operand.operator.symbol + operand.operand;
        return calculate(expression, "estimate");
    }

    private Operand getMaxWeight(List<Operand> operandList) {
        if (operandList.size() == 0) return null;

        Operand max = null;
        for (Operand operand : operandList) {
            int weight = operand.operator.weight;
            if (max == null || Math.max(max.operator.weight, weight) == weight) {
                max = operand;
            }
        }

        return max;
    }

}
