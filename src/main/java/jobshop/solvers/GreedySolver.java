package jobshop.solvers;

import jobshop.Instance;
import jobshop.Result;
import jobshop.Solver;
import jobshop.encodings.JobNumbers;
import jobshop.encodings.Task;
import jobshop.encodings.ResourceOrder;

import java.util.*;

public class GreedySolver implements Solver {
    @Override
    public Result solve(Instance instance, long deadline) {

        HashSet<Task> monHashSet = new HashSet<Task>();

        ResourceOrder Resultat = new ResourceOrder(instance);

        Task next = new Task(0,0);

        int duration = 0 ;

         for(int i = 0 ; i < instance.numJobs ; i++) {
             monHashSet.add(new Task (i,0));
         }
        int lastDuration =  Integer.MAX_VALUE;
         while( !monHashSet.isEmpty())
         {
             lastDuration =  Integer.MAX_VALUE;
             for (Task t : monHashSet)
             {
                 duration =  instance.duration(t.job,t.task);
                 if (duration < lastDuration)
                 {
                     lastDuration = duration;

                     next = t;

                 }
             }
             Resultat.addTask(instance.machine(next),next);
             monHashSet.remove(next);
             if ( next.task != instance.numTasks -1) {
                 monHashSet.add(new Task(next.job,next.task+1));
             }
         }
        return new Result(instance, Resultat.toSchedule(), Result.ExitCause.Blocked);
    }
}

