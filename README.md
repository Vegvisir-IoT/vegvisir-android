Vegvisir Android
---

### Overview

This is a Android implementation for [Vegvisir Project](https://vegvisir.cs.cornell.edu/). As the origin 
Vegvisir project designed in a layered structure, this implementation mimics the ideas in the paper splitting 
the Vegvisir Android infrastructure into the following layers:

- App layer
  - Currently, this layer includes 2 applications, *Annotative Map* and *Task List*. 
- Pub/Sub layer
  - Multiplexing transactions to the correct applications and letting applications create new transactions.
- Core layer
  - This is the implementation of the block layer and reconciliation layer in the paper.
- Gossip layer
  - The layer performs selecting the next peer to reconcile with.
- Network layer
  - It includes a Google Nearby based implementation for disconnected network.
  - and, another TCP based implementation for communicating with peers through base station or the internet.

### Build & Run

To run the program, first, one needs to install [Android Studio](https://developer.android.com/studio).
Then, open and run this project from Android Studio. 

### Applications Inside
There are two applications can run on android devices with
API level >= 28. The first application is *Annotative Map* that allows users put a noted marker on a shared map. This
is useful for scenarios where, the infrastructure network is missing and GPS location is not trustable. For instance,
when first responders need to enter a building to save lives, then this can help.  The other application is *Task List*,
which lets users to create a shared task list between team members who have devices running Vegvisir on a peer-to-peer 
ad hoc network.