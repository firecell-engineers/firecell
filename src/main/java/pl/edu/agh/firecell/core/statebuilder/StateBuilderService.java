package pl.edu.agh.firecell.core.statebuilder;

import io.reactivex.rxjava3.core.BackpressureOverflowStrategy;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.processors.PublishProcessor;
import io.reactivex.rxjava3.schedulers.Schedulers;
import org.joml.Vector3i;
import pl.edu.agh.firecell.model.State;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class StateBuilderService {
    private final PublishProcessor<List<ElementWrapper>> pp = PublishProcessor.create();
    private final StateBuilder stateBuilder;
    private Disposable subscription;
    private State currentState;

    public StateBuilderService(Vector3i spaceSize) {
        this.stateBuilder = new StateBuilder(spaceSize);
        calculateState(Collections.emptyList());
        initializeSubscription();
    }

    private void initializeSubscription() {
        subscription = pp.onBackpressureBuffer(1, () -> {
                }, BackpressureOverflowStrategy.DROP_OLDEST)
                .observeOn(Schedulers.single())
                .subscribe(this::calculateState);
    }

    public State getCurrentState() {
        return currentState;
    }

    public void scheduleStateCalculation(List<ElementWrapper> elements) {
        pp.onNext(new ArrayList<>(elements));
    }

    private void calculateState(List<ElementWrapper> elements) {
        stateBuilder.clear();
        elements.stream()
                .map(ElementWrapper::element)
                .forEach(stateBuilder::addElement);
        currentState = stateBuilder.build();
    }

    public void dispose() {
        subscription.dispose();
    }

    public void setSpaceSize(Vector3i spaceSize) {
        stateBuilder.setSpaceSize(spaceSize);
    }
}
