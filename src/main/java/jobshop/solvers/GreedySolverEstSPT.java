package jobshop.solvers;

import jobshop.Instance;
import jobshop.Result;
import jobshop.Solver;
import jobshop.encodings.ResourceOrder;
import jobshop.encodings.Task;

import java.util.*;

public class GreedySolverEstSPT  implements Solver{



    @Override
    public Result solve(Instance instance, long deadline) {

        int startTime[] = new int[instance.numJobs];
        int machineRelease[] = new int [instance.numMachines];
        int min, duration;
        HashSet<Task> monHashSet = new HashSet<Task>();
        HashSet<Task> monHashSetReduce = new HashSet<Task>();
        ResourceOrder Resultat = new ResourceOrder(instance);
        Task next = null;

        Arrays.fill(machineRelease,0);
        // initiaisation : Toutes les premieres tache de chaque jobs + timer a zero
        for(int i = 0 ; i < instance.numJobs ; i++) {
            monHashSet.add(new Task (i,0));
            startTime[i] = 0;
        }
        int lastDuration =  Integer.MAX_VALUE;


        while( !monHashSet.isEmpty())
        {
            monHashSetReduce.clear();
            min = -1;
            next = null;
            duration =  0;

            for ( Task t : monHashSet) {
                if( min == -1)
                {
                    min = Math.max(startTime[t.job],machineRelease[instance.machine(t)]);
                    monHashSetReduce.add(t);
                }
                else
                {
                    int minD = Math.max(startTime[t.job],machineRelease[instance.machine(t)]);
                    if(minD == min)
                    {
                        monHashSetReduce.add(t);
                    }
                    if (minD < min )
                    {
                        monHashSetReduce.clear();
                        monHashSetReduce.add(t);
                        min = minD;
                    }
                }
            }


            lastDuration =  Integer.MAX_VALUE;

            for (Task t : monHashSetReduce)
            {
                    duration = instance.duration(t.job, t.task);
                    if (duration < lastDuration)
                    {
                        lastDuration = duration;
                        next = t;
                    }
            }


            machineRelease[instance.machine(next)] = min + instance.duration(next);
            monHashSet.remove(next);

            if (next.task != (instance.numTasks-1)) {
                startTime[next.job] = min + instance.duration(next.job,next.task);
                monHashSet.add(new Task(next.job,next.task+1));
            }
            Resultat.addTask(instance.machine(next),next);
        }
        return new Result(instance, Resultat.toSchedule(), Result.ExitCause.Blocked);
    }
}

