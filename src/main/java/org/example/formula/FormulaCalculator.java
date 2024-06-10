package org.example.formula;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.logging.Logger;

public class FormulaCalculator extends Formula {
    private static final Logger log = Logger.getLogger(FormulaCalculator.class.getName());
    public FormulaCalculator(){
    }
    /**
     * Метод для вычисления формулы для подстановки закладки
     * @return вычисленная строка
     */
    public String calculate() {
        //проверка, заданы ли обязательные параметры
        if (checkMandatoryParams()){
            StringBuilder sb = new StringBuilder();
            Field[] fields = Formula.class.getDeclaredFields();
            //начинается со 2, тк первые 2 это разделители
            for(int i = 2; i < fields.length; i++){
                try {
                    sb.append(fields[i].getName());
                    sb.append(TITLE_SPLIT);
                    sb.append(fields[i].get(this));

                    //если нужно оставить старую стилизацию
                    if (fields[i].getName().equals("saveOldStyle") && fields[i].get(this).equals(true))
                        break;
                }catch (IllegalAccessException ex){
                    log.info(Arrays.toString(ex.getStackTrace()));
                }

                if(i < fields.length - 1)
                    sb.append(PARAM_SPLIT);
            }
            return sb.toString();
        } else throw new IllegalStateException("Not all required parameters are set");
    }
}