package jobshop.solvers;

import jobshop.Instance;
import jobshop.Result;
import jobshop.Solver;
import jobshop.encodings.ResourceOrder;
import jobshop.encodings.Task;

import java.util.*;

public class TabooSolver implements Solver {

    /** A block represents a subsequence of the critical path such that all tasks in it execute on the same machine.
     * This class identifies a block in a ResourceOrder representation.
     *
     * Consider the solution in ResourceOrder representation
     * machine 0 : (0,1) (1,2) (2,2)
     * machine 1 : (0,2) (2,1) (1,1)
     * machine 2 : ...
     *
     * The block with : machine = 1, firstTask= 0 and lastTask = 1
     * Represent the task sequence : [(0,2) (2,1)]
     *
     * */
    static class Block {
        /** machine on which the block is identified */
        final int machine;
        /** index of the first task of the block */
        final int firstTask;
        /** index of the last task of the block */
        final int lastTask;

        Block(int machine, int firstTask, int lastTask) {
            this.machine = machine;
            this.firstTask = firstTask;
            this.lastTask = lastTask;
        }
    }

    /**
     * Represents a swap of two tasks on the same machine in a ResourceOrder encoding.
     *
     * Consider the solution in ResourceOrder representation
     * machine 0 : (0,1) (1,2) (2,2)
     * machine 1 : (0,2) (2,1) (1,1)
     * machine 2 : ...
     *
     * The swam with : machine = 1, t1= 0 and t2 = 1
     * Represent inversion of the two tasks : (0,2) and (2,1)
     * Applying this swap on the above resource order should result in the following one :
     * machine 0 : (0,1) (1,2) (2,2)
     * machine 1 : (2,1) (0,2) (1,1)
     * machine 2 : ...
     */
    static class Swap {
        // machine on which to perform the swap
        final int machine;
        // index of one task to be swapped
        final int t1;
        // index of the other task to be swapped
        final int t2;

        Swap(int machine, int t1, int t2) {
            this.machine = machine;
            this.t1 = t1;
            this.t2 = t2;
        }

        /** Apply this swap on the given resource order, transforming it into a new solution. */
        public void applyOn(ResourceOrder order) {
            ResourceOrder Cop = order.copy();
            order.tasksByMachine[this.machine][this.t1] = Cop.tasksByMachine[this.machine][this.t2];
            order.tasksByMachine[this.machine][this.t2] = Cop.tasksByMachine[this.machine][this.t1];
        }
    }


    @Override
    public Result solve(Instance instance, long deadline) {

        GreedySolverEstLRPT GR= new GreedySolverEstLRPT(); // Generer une solution initiale réalisatble
        Result resInit = GR.solve(instance,deadline);
        ResourceOrder s = new ResourceOrder(resInit.schedule); // Solution courante
        ResourceOrder sEtoile = new ResourceOrder(resInit.schedule); // sauvegarde de la meilleur solution

        int t = instance.numJobs * instance.numMachines ;
        int sTaboo[][] = new int [t][t];

        for(int i =0 ; i < t ; i++)
        {
            Arrays.fill(sTaboo[i],0);
        }

        //System.out.println("Setoile : " + sEtoile);
        int k = 0; // Compteur itération
        int maxIter = 750;
        int dureeTaboo = 10;
       // boolean check = true;


        while(/*check && */((deadline - System.currentTimeMillis() > 1)) && (k < maxIter)) {
            k++;
            List<Block> LB =  blocksOfCriticalPath(s);
            ResourceOrder sPrime = null;
            ResourceOrder sPPrime = null;
            //ArrayList<ResourceOrder> voisin = new ArrayList<ResourceOrder>();
            Swap swapp = null;

            int makespan = Integer.MAX_VALUE;
            for( Block blo : LB )
            {
                List<Swap> swap = neighbors(blo);
                for(Swap sw : swap)
                {
                    if(sTaboo[sw.t1][sw.t2] < k)
                    {
                        sPPrime = s.copy();
                        sw.applyOn(sPPrime);

                        if (sPPrime.toSchedule().makespan() < makespan )
                        {
                            makespan = sPPrime.toSchedule().makespan();

                            if((sPrime == null )||( sPrime.toSchedule().makespan() > makespan))
                            {
                                sPrime = sPPrime.copy();
                                swapp = sw;
                            }
                        }
                    }
                }
            }
            if(swapp != null)
                sTaboo[swapp.t1][swapp.t2] = k+ dureeTaboo; // On l'a rend taboo


            if(sPrime != null)
            {
                s = sPrime.copy();
                if(sPrime.toSchedule().makespan() < sEtoile.toSchedule().makespan())
                    sEtoile = sPrime.copy();
            }
        }
        return new Result(instance, sEtoile.toSchedule(), Result.ExitCause.Blocked);
    }

    public List<Block> blocksOfCriticalPath(ResourceOrder order)
    {
        List<Task> Path = order.toSchedule().criticalPath();
        ArrayList<Block> BlockList = new ArrayList<Block>() ;
        int start = -1;
        int machine = -1;
        Task last = null;
        for ( Task t : Path)
        {
            if(start == -1)
            {
                start = Path.indexOf(t);
                machine = order.toSchedule().pb.machine(t);
            }
            else if((machine != order.toSchedule().pb.machine(t)))
            {
                if ( Path.indexOf(t)-1-start > 0)
                {
                    BlockList.add(new Block(machine,getIndex(order.tasksByMachine[machine],last)-( Path.indexOf(last)-start),getIndex(order.tasksByMachine[machine],last)));
                }
                machine = order.toSchedule().pb.machine(t);
                start = Path.indexOf(t);
            }
            else if  (Path.indexOf(t) == Path.size()-1)
            {
                if ( Path.indexOf(t)-start > 0)
                {

                    BlockList.add(new Block(machine,getIndex(order.tasksByMachine[machine],t)-( Path.indexOf(t)-start),getIndex(order.tasksByMachine[machine],t)));
                }

            }
            last = t;
        }
        return BlockList;
    }

    /** For a given block, return the possible swaps for the Nowicki and Smutnicki neighborhood */
    List<Swap> neighbors(Block block) {
        ArrayList<Swap> listSwap = new ArrayList<Swap>();
        if (block.lastTask - block.firstTask > 1) {
            listSwap.add(new Swap(block.machine,block.firstTask,block.firstTask+1));
            listSwap.add(new Swap(block.machine,block.lastTask,block.lastTask-1));
        }
        else
        {
            listSwap.add(new Swap(block.machine,block.firstTask,block.lastTask));
        }

        return listSwap;
    }

    int getIndex (Task[] a, Task t)
    {
        for(int i = 0 ; i < a.length ; i ++) {
            if (a[i].equals(t)) {
                return i ;
            }
        }
        return -1;
    }
}