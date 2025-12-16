package collections;

import java.util.LinkedList;

/**
 * Класс описывает работу простейшей очереди по приоритету, которая работает по принципу FIFO
 * @author Akulich DI
 * @version 0.1A
 */

public class PriorityQueue {

    /**
     * Хранение задания осуществляется в коллекции типа LInkedList
     */

    private LinkedList<Task> tasks = new LinkedList<>();

    /**
     * Метод принимает на вход заявку и добавляет ее в очередь.
     * Если встречаются 2 задания с одинаковым приоритетом, то в очереди
     * они распределяются по принципу FIFO
     * @param task задача, которая добавляется в очередь
     */
    public void put (Task task ){
        int i = 0;
        for (Task current : tasks){
            if (task.getPriority()< current.getPriority()){
                break;
            }
            i++;
        }
        tasks.add(i, task);
    }

    /**
     * Метод позволяет получить первую задачу в очереди
     * @return возвращает задачу из головы очереди или null если очередь пустая
     */
    public Task take(){
        return  this.tasks.poll();
    }


}
