package jobshop.solvers;

import jobshop.Instance;
import jobshop.Result;
import jobshop.Solver;
import jobshop.encodings.ResourceOrder;
import jobshop.encodings.Task;

import java.util.*;

public class GreedySolverEstLRPT implements Solver {
    public Result solve(Instance instance, long deadline) {

        int startTime[] = new int[instance.numJobs];
        int machineRelease[] = new int [instance.numMachines];
        int min, duration;
        Arrays.fill(machineRelease,0);
        HashSet<Task> monHashSet = new HashSet<Task>();
        HashSet<Task> monHashSetReduce = new HashSet<Task>();
        ResourceOrder Resultat = new ResourceOrder(instance);
        Task next = null;
        int [] RemainingTime = new int[instance.numJobs ];

        // initialisation Remaining Time
        for( int i = 0 ; i< instance.numJobs  ; i ++) {
            for(int j = 0 ; j < instance.numTasks ; j++) {
                RemainingTime[i] += instance.duration(i,j);
            }
        }

        for(int i = 0 ; i < instance.numJobs ; i++) {
            monHashSet.add(new Task (i,0));
            startTime[i] = 0;
        }

        while( !monHashSet.isEmpty() & (deadline - System.currentTimeMillis() > 1)) {
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

            for ( Task t : monHashSetReduce) {
                if(next == null) {
                    next = t;
                    duration = RemainingTime[t.job];
                }
                else if (duration < RemainingTime[t.job] ) {
                    next = t;
                    duration = RemainingTime[t.job];
                }
            }


            RemainingTime[next.job] -= instance.duration(next.job, next.task) ;
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

