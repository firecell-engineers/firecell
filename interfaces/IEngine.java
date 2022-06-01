interface IState {
    interface ICell {}
}

interface IAlgorithm {
    // Perhabs only one of those methods will be used, depending on the implementation of the algorithm 

    /**
     * Calculates the next state of a cell based on the current state of the cell
     * @param state
     * @param cell
     */
    IState calculate(IState state, ICell cell);

    /**
     * Calculates the next state of the whole environment based on its current state
     * @param state
     */
    IState calculate(IState state);
}

interface IConfig {}

interface IStateLintener { void putState(IState state); }

interface IEngine {
    /**
     * @param initState the state of the environment - room, materials, etc.
     */
    void setInitState(IState initState);

    /**
     * @param algorithm the algorithm used to determine how to compute next state from the current one
     */
    void setAlgorithm(IAlgorithm algorithm);

    /**
     * Provides information required for the simulation to run. E.g. GPU or CPU, how many frames per second, etc.
     * 
     * @param config the configuration object (or file)
     */
    void setConfig(IConfig config);

    /**
     * Sets a listener, which will consume a state, that will be created by the engine.
     * The Storage object could be such a listener, having a method 'put'
     * 
     * @param listener
     */
    void subscribeStateListener(IStateLintener listener);

    /**
     * Starts the simulation on new thread
     */
    void start();

    /**
     * Ends the simulation
     */
    void stop();

    /**
     * Pauses the simulation
     */
    void pause();

    /**
     * 
     * @return
     */
    SimulationPhase getSimulationPhase();
}

class Engine implements IEngine {
    private IState          currentState;
    private IAlgorithm      algorithm;
    private IConfig         config;
    private IStateLintener  listener;
    private SimulationPhase simulationPhase;
    private boolean         isRunning;

    Engine() { isRunning = true; }

    @Override
    public void setInitState(IState initState) { this.initState = initState; }

    @Override
    public void setAlgorithm(IAlgorithm algorithm) { this.algorithm = algorithm; }

    @Override
    public void setConfig(IConfig config) { this.config = config; }

    @Override
    public void subscribeStateListener(IStateLintener listener) { this.listener = listener; }

    /**
     * Depending on CPU / GPU implementation, executes computation on particular cells
     * @return next state
     */
    private IState computeNextState() { return null; }

    private void run() {
        while (isRunning) {
            IState nextState = computeNextState();

            listener.putState(nextState);
            currentState = nextState;
        }
    }

    @Override
    public void start() { 
        isRunning = true;
        
        run();
    }

    @Override
    public void stop() { isRunning = false; }

    @Override
    public SimulationPhase getSimulationPhase() { return simulationPhase; }
}