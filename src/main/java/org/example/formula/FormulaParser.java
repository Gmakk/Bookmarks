package org.example.formula;

import org.example.BookmarksReplaceWithText;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
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
    public void parse(String formula) throws IllegalArgumentException, IllegalAccessException {
        //регулярное выражение для разделения формулы на параметры, находящиеся между разделительным знаком
        if(formula == null || formula.isBlank())
            throw new IllegalArgumentException("formula is blank or null");
        Pattern pattern = Pattern.compile(SPLIT);
        String[] params = pattern.split(formula,-1);
        for (String par : params)
            log.info(par);

        //заполняем поля разбитыми строками
        //получаем список всех полей
        List<Field> fields = new ArrayList<>();
        fields.addAll(Arrays.asList(Formula.class.getDeclaredFields()));

        //удаляем первое поле, тк там хранится разделяющий элемент который уже задан
        fields.remove(0);

        //тк параметры в формуле находятся в то же порядке, что и поля, то присваиваем i-тому полю значение i-того параметра
        //TODO:проверка валидности формулы
        for(int i=0;i<params.length;i++){
            if(fields.get(i).getName().equals("primaryKeyValue")) {
                fields.get(i).set(this,params[i]);
                continue;
            }
            if(params[i].equals("true") || params[i].equals("false"))
                fields.get(i).set(this,Boolean.valueOf(params[i]));
            else if (params[i].matches("-?\\d+(\\.\\d+)?"))
                fields.get(i).set(this,Integer.valueOf(params[i]));
            else
                fields.get(i).set(this,params[i]);
        }
    }
}
