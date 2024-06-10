package org.example.formula;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class FormulaParser extends Formula{
    protected static Logger log = LoggerFactory.getLogger(Formula.class);

    /**
     * Метод, заполняющий поля класса параметрами, указанными в формуле
     * @param formula формула для разбора
     * @throws IllegalArgumentException если формула не соответствует шаблону
     */
    public void parse(String formula){
        //регулярное выражение для разделения формулы на параметры, находящиеся между разделительным знаком
        if(formula == null || formula.isBlank())
            throw new IllegalArgumentException("formula is blank or null");
        Pattern paramPattern = Pattern.compile(PARAM_SPLIT);
        String[] params = paramPattern.split(formula,-1);
        Pattern titlePattern = Pattern.compile(TITLE_SPLIT);

        String paramName;
        String paramValue;
        Field currentField;

        for (String param : params) {
            paramName = titlePattern.split(param)[0];
            paramValue = titlePattern.split(param)[1];

            try {
                currentField = Formula.class.getDeclaredField(paramName);

                if (currentField.getType().equals(Boolean.class))
                    currentField.set(this, Boolean.valueOf(paramValue));
                else if (currentField.getType().equals(Integer.class))
                    currentField.set(this, Integer.valueOf(paramValue));
                else
                    currentField.set(this, paramValue);

            } catch (NoSuchFieldException ex) {
                System.err.println();
                log.info("Field " + paramName + " not found\n" + Arrays.toString(ex.getStackTrace()));
            } catch (IllegalAccessException ex) {
                log.info(Arrays.toString(ex.getStackTrace()));
            }
        }

        if(!checkMandatoryParams())
            throw new IllegalArgumentException("Formula does not contain all required fields");
    }
}
