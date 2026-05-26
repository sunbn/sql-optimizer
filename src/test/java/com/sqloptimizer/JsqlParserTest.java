package com.sqloptimizer;

import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.util.TablesNamesFinder;

public class JsqlParserTest {

    public static void main(String[] args) throws Exception {
        java.lang.reflect.Method method = TablesNamesFinder.class.getMethod("getTableList", Statement.class);
        System.out.println("Return type: " + method.getReturnType());
        System.out.println("Generic return type: " + method.getGenericReturnType());

        Statement statement = CCJSqlParserUtil.parse("SELECT * FROM users u JOIN orders o ON u.id = o.user_id WHERE u.name = 'test'");
        Object result = new TablesNamesFinder().getTableList(statement);
        System.out.println("Result type: " + result.getClass());
        System.out.println("Result: " + result);
    }
}