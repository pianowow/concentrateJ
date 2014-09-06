package test.java;

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.StreamHandler;

public class MyConsoleHandler extends StreamHandler {           
    private java.util.logging.Formatter formatter = new SingleLineFormatter();
     public void publish(LogRecord record){      
         if(record.getLevel().intValue() < Level.WARNING.intValue())
             System.out.print(formatter.format(record));            
         else
             System.err.print(formatter.format(record));
     }
}