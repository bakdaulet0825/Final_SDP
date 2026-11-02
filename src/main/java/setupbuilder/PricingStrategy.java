package com.setupbuilder;

public interface PricingStrategy {
    double calculatePrice(double basePrice);
    String getStrategyName();
}

class StandardPricingStrategy implements PricingStrategy {
    @Override
    public double calculatePrice(double basePrice) {
        return basePrice;
    }

    @Override
    public String getStrategyName() {
        return "Standard Price (No Discount)";
    }
}

class StudentDiscountStrategy implements PricingStrategy {
    @Override
    public double calculatePrice(double basePrice) {
        return basePrice * 0.90; // 10% discount
    }

    @Override
    public String getStrategyName() {
        return "Student Special Discount (-10%)";
    }
}

class InstallmentPlanStrategy implements PricingStrategy {
    private int months;

    public InstallmentPlanStrategy(int months) {
        this.months = months;
    }

    @Override
    public double calculatePrice(double basePrice) {
        double feePercent = (months == 12) ? 0.12 : 0.05;
        return (basePrice * (1 + feePercent)) / months; // Monthly payment
    }

    @Override
    public String getStrategyName() {
        return "Installment Plan (" + months + " Months)";
    }
}
