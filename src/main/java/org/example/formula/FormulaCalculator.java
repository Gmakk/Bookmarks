package org.example.formula;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Base64;
import java.util.logging.Logger;

public class FormulaCalculator extends Formula {
    private static final Logger log = Logger.getLogger(FormulaCalculator.class.getName());

    public FormulaCalculator(){
    }

    /**
     * Метод для вычисления формулы на основе полей экземпляра класса
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
                    //логин и пароль кодируются base64, чтобы не хранить их в открытом виде
                    if(fields[i].getName().equals("username") || fields[i].getName().equals("password")){
                        sb.append(Base64.getEncoder().encodeToString(((String)fields[i].get(this)).getBytes()));
                    }else {
                        sb.append(fields[i].get(this));
                        //если нужно оставить старую стилизацию, то остальные поля не учитываем
                        if (fields[i].getName().equals("saveOldStyle") && fields[i].get(this).equals(true))
                            break;
                    }
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