package com.setupbuilder;

import java.util.ArrayList;
import java.util.List;

public interface BuildObserver {
    void onBuildUpdated(PCBuild build, String changeMessage);
}

class PriceAlertObserver implements BuildObserver {
    @Override
    public void onBuildUpdated(PCBuild build, String changeMessage) {
        System.out.println("[NOTIF / OBSERVER] Build '" + build.getName() + "' updated: "
                + changeMessage + ". New base total: $" + build.getBaseTotal());
    }
}

class BuildSubject {
    private List<BuildObserver> observers = new ArrayList<>();

    public void attach(BuildObserver observer) {
        observers.add(observer);
    }

    public void detach(BuildObserver observer) {
        observers.remove(observer);
    }

    public void notifyObservers(PCBuild build, String message) {
        for (BuildObserver obs : observers) {
            obs.onBuildUpdated(build, message);
        }
    }
}
