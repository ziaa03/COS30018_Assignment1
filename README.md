# COS30018_Assignment1

To run config:

-gui -agents "customerAgent:allAgents.CustomerAgent;masterroutingAgent:allAgents.MasterRoutingAgent;deliveryAgent:allAgents.DeliveryAgent;deliveryAgent1:allAgents.DeliveryAgent1;deliveryAgent2:allAgents.DeliveryAgent2;deliveryAgent3:allAgents.DeliveryAgent3"

CURRENT PROGRESS: (AS OF 26/4)
- customerAgent as it is now able to generate 10 parcels per tick
- customerAgent is able to send all the 10 parcel information to MRA in one go
- MRA is able to receive the 10 parcels
- but MRA still assigns each parcel to a random DA to deliver one by one instead of sending all 10 parcels to one DA only


PLAN LAYOUT: 
- step 1: CA generates more than 10 parcels 
- step 2: mra broadcasts to all agents the parcels involved at diff location points (summation of parcel weight)
- step 3: da receives capacity request from mra (then responds with their capacity)
- step 4: mra considers da's capacity and sends a call for proposal to agents
- step 5: all agents either accept or reject parcel delivery based on total weight, the agents who cant take everything will reject also
- step 6: mra makes the decision to decide who to give to based on highest capacity and if that agent rejects, give to the next
