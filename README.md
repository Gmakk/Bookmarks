# Приложение для заполнения документов из базы данных
Работа приложения основана на механизме закладок в документах Microsoft Word. Создавалось с целью автоматизировать заполнение шаблонных документов, имеющимися в бд данными. 

Для редактирования документов использовалась библиотека doc4j. Интерфейс сделан с помощью JavaFX

```
Для запуска:
clean javafx:run
Для отладки:
clean javafx:run@debug
```


### Перед началом работы с документом его необходимо подготовить
1. Выставить закладки в места подстановки информации
2. В программе добавить к созданным закладкам формулы, определяющие откуда и в каком виде подставлять информацию
### Поддерживаемые БД
1. PostgreSQL
2. MySQL
3. Derby
### Инструкции
1. [Видео-пример](https://disk.yandex.ru/i/tIDXuHuM8qq7Fw) работы
2. [Руководство пользователя](https://disk.yandex.ru/i/35oQtmi1tfGkgg)
### Скрины приложения
![image](https://github.com/user-attachments/assets/8104845d-dee4-47ba-8e34-4d27dea5908e)
![image](https://github.com/user-attachments/assets/09230ddc-60b9-4aac-8f8f-d5d2a5e6a10f)
![image](https://github.com/user-attachments/assets/955bf994-b56a-4b07-8042-64b26374cbaa)