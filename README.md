# Computer_Architecture_Course_Project
The description of project:
From high level view, the project follows the following control flow - read script file program and a while loop to execute Tomasulo Algorithm.
The read file program is aim to get the memory content and store in memory unit, get all instructions with corresponding address and store in instruction cache 
and get all branch target with corresponding address and store in branch target buffer.
I use a while loop to keep Tomasulo algorithm running until there is no more branch fetched from instruction unit and there is no instructions laid in ROB.

In the loop, the program follows reverse order-commit,write back,execute and fetch.

The fetch is done by several components -Instruction Cache,Instruction Unit, Decode Unit, Instruction Queue Register File.
At each cycle, the instruction Unit will Fetch NF instructions from instruction cache and store into Decode unit. 
The Decode unit will decode un-decoded instructions into string array with size 4 and then do Register Renaming techniques.
After register renaming, Decode unit will store the cleaned instruction to Instruction Queue.
The instruction Queue is responsible for issuing at maximum NW=4 instructions to Reservation Station. 
It will issue the instruction if there is empty spot in Reservation Station and empty spot in ROB, Otherwise it will stop.

The execution is done by reservation station, it will execute instruction when both operands ready.If not ready, reservation station will watch CDB for result.
For load buffer,after calculation the address,it will watch ROB. 
If there is a store with same address the load buffer will get the value immediately. 
If there is a load with different address,it will bypass the instruction. 
If it is an unknown address for other store,it will stop and wait until the calculation of address finishes.
After scan the ROB, if load buffer still cannot find value, it will get value from memory.
For store buffer,after address calculation it will pass value to ROB.

The write back is also done by reservation station, it write value on Common Data Bus to all awaiting FUs and reorder buffer and then mark reservation station available.

The commit is done by ROB. For each instruction, there is three types of commitment.
The first type is Normal Commit : add, addi, fadd, fsub, fmul, fdiv fld. The ROB will Update the Register
The second type is Store commit : fsd. The ROB will Update the Memory.
The third type is Branch Prediction (bne), the ROB will flush everything. 
It is worth to mention that, when branch predictor make its prediction, I will store the pc for another branch and also the snap shot of freelist and maptable in a linked list fashion.
When my branch prediction is wrong, the first thing I do is recover from PC from otherway PC (the linked list data structure I mention above),
and then the ROB will flush everything inside, the reservation station will flush everything, the Common data bus will flush everything. 
For freelist and maptable, they will recovery from the snapshot.

How to run the project?
Run the simulator.



