package org.example.formula;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
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
        if(formula == null || formula.isBlank())
            throw new IllegalArgumentException("formula is blank or null");
        //регулярное выражение для разделения формулы на параметры, находящиеся между разделительным знаком
        Pattern paramPattern = Pattern.compile(PARAM_SPLIT);
        String[] params = paramPattern.split(formula,-1);
        //для разделения параметра на его имя и значение
        Pattern titlePattern = Pattern.compile(TITLE_SPLIT);

        String paramName;
        String paramValue;
        Field currentField;

        for (String param : params) {
            paramName = titlePattern.split(param)[0];
            //логин и пароль кодируются, чтобы не хранить их в открытом виде
            if(paramName.equals("username") || paramName.equals("password")) {
                //если логин или пароль пустые
                if((paramName+Formula.TITLE_SPLIT).equals(param))
                    paramValue = "";
                //логин или пароль не пустые
                else
                    paramValue = new String(Base64.getDecoder().decode(titlePattern.split(param)[1]));
            }
            else
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
                log.info("Field " + paramName + " not found\n" + Arrays.toString(ex.getStackTrace()));
            } catch (IllegalAccessException ex) {
                log.info(Arrays.toString(ex.getStackTrace()));
            }
        }

        if(!checkMandatoryParams())
            throw new IllegalArgumentException("Formula does not contain all required fields");
    }
}
