package jobshop.solvers;

import jobshop.Instance;
import jobshop.Result;
import jobshop.Solver;
import jobshop.encodings.ResourceOrder;
import jobshop.encodings.Task;

import java.util.*;

public class GreedySolverLRPT implements Solver {
    @Override
    public Result solve(Instance instance, long deadline) {

        int end = 0;
        int duration = 0 ;
        int indice = 0;
        int indiceC = 0;
        HashSet<Task> monHashSet = new HashSet<Task>();
        ResourceOrder Resultat = new ResourceOrder(instance);
        Task next = new Task(0,0);
        int [] RemainingTime = new int[instance.numJobs ];

        // initialisation Remaining Time
        for( int i = 0 ; i< instance.numJobs  ; i ++)
        {
            for(int j = 0 ; j < instance.numTasks ; j++)
            {
                RemainingTime[i] += instance.duration(i,j);
            }
        }

        for(int i = 0 ; i < instance.numJobs ; i++) {
            monHashSet.add(new Task (i,0));
        }
        int size = instance.numJobs;

        while( !monHashSet.isEmpty())
        {
            // System.out.println("Nouvelle boucle while");

            duration = 0;
            for ( int i = 0; i< instance.numJobs; i++)
            {
                if( duration < RemainingTime[i])
                {
                    duration = RemainingTime[i];
                    indice = i;
                }
            }

            for(int j = 0 ; j <= size ; j++)
            {
                if(monHashSet.contains(new Task(indice,j)))
                {
                    indiceC = j;
                    next = new Task(indice,indiceC);
                }
            }

            RemainingTime[indice] -= instance.duration(indice, indiceC)  ;
            Resultat.addTask(instance.machine(next),next);
            monHashSet.remove(next);

            if ( next.task != instance.numTasks -1) {
                monHashSet.add(new Task(next.job,next.task+1));
            }
        }


        return new Result(instance, Resultat.toSchedule(), Result.ExitCause.Blocked);
    }
}

