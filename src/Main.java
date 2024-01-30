import java.util.HashMap;
import java.util.Map;

class ATMException extends Exception {
    public ATMException(String message) {
        super(message);
    }
}

class ATMInitializationError extends ATMException {
    public ATMInitializationError(String message) {
        super(message);
    }
}

class ATMWithdrawalError extends ATMException {
    public ATMWithdrawalError(String message) {
        super(message);
    }
}

class ATM {
    private final Map<Integer, Integer> denominations;
    private final int minWithdrawalAmount;
    private final int maxNotesToDispense;
    private final Map<Integer, Integer> availableNotes;

    public ATM(int[] denominations, int minWithdrawalAmount, int maxNotesToDispense) {
        this.denominations = new HashMap<>();
        this.availableNotes = new HashMap<>();
        for (int denomination : denominations) {
            this.denominations.put(denomination, 0);
            this.availableNotes.put(denomination, 0);
        }
        this.minWithdrawalAmount = minWithdrawalAmount;
        this.maxNotesToDispense = maxNotesToDispense;
    }

    public void initializeATM(Map<Integer, Integer> notes) throws ATMInitializationError {
        for (Map.Entry<Integer, Integer> entry : notes.entrySet()) {
            int denomination = entry.getKey();
            int count = entry.getValue();
            if (!denominations.containsKey(denomination) || count < 0) {
                throw new ATMInitializationError("Invalid denomination or count for ATM initialize");
            }
            availableNotes.put(denomination, count);
        }
    }

    public void inputMoney(Map<Integer, Integer> notes) throws ATMException {
        for (Map.Entry<Integer, Integer> entry : notes.entrySet()) {
            int denomination = entry.getKey();
            int count = entry.getValue();
            if (!denominations.containsKey(denomination) || count < 0) {
                throw new ATMException("Invalid denomination or count for input money");
            }
            availableNotes.put(denomination, availableNotes.get(denomination) + count);
        }
    }

    public Map<Integer, Integer> withdrawMoney(int amount) throws ATMWithdrawalError {
        if (amount % minWithdrawalAmount != 0) {
            throw new ATMWithdrawalError("Amount should be multiple of minimal withdraw amount");
        }

        int totalAmount = amount;
        Map<Integer, Integer> withdrawalNotes = new HashMap<>();

        for (int denomination : denominations.keySet()) {
            int noteCount = totalAmount / denomination;
            noteCount = Math.min(noteCount, availableNotes.get(denomination));
            noteCount = Math.min(noteCount, maxNotesToDispense);

            if (noteCount > 0) {
                withdrawalNotes.put(denomination, noteCount);
                totalAmount -= denomination * noteCount;
                availableNotes.put(denomination, availableNotes.get(denomination) - noteCount);
            }

            if (totalAmount == 0) {
                break;
            }
        }

        if (totalAmount != 0) {
            throw new ATMWithdrawalError("Unable to give exact amount of money");
        }

        try {
            inputMoney(withdrawalNotes);
        } catch (ATMException e) {
            throw new ATMWithdrawalError("Revert changes failed");
        }

        return withdrawalNotes;
    }

    public Map<Integer, Integer> getAvailableNotes() {
        return availableNotes;
    }
}

class BankException extends Exception {
    public BankException(String message) {
        super(message);
    }
}

class Bank {
    private ATM[] atms;
    private int totalMoney;

    public Bank() {
        this.atms = new ATM[0];
        this.totalMoney = 0;
    }

    public void addATM(ATM atm) {
        ATM[] newATMs = new ATM[atms.length + 1];
        System.arraycopy(atms, 0, newATMs, 0, atms.length);
        newATMs[atms.length] = atm;
        atms = newATMs;
        totalMoney += atm.getAvailableNotes().values().stream().mapToInt(Integer::intValue).sum();
    }

    public void removeATM(ATM atm) {
        for (int i = 0; i < atms.length; i++) {
            if (atms[i] == atm) {
                ATM[] newATMs = new ATM[atms.length - 1];
                System.arraycopy(atms, 0, newATMs, 0, i);
                System.arraycopy(atms, i + 1, newATMs, i, atms.length - i - 1);
                atms = newATMs;
                totalMoney -= atm.getAvailableNotes().values().stream().mapToInt(Integer::intValue).sum();
                break;
            }
        }
    }

    public int totalMoneyInNetwork() {
        return totalMoney;
    }
}

public class Main {
    public static void main(String[] args) {
        try {
            ATM atm1 = new ATM(new int[]{1, 2, 5, 10, 20, 50, 100, 200, 500}, 10, 100);
            atm1.initializeATM(Map.of(1, 100, 10, 50, 100, 20));

            ATM atm2 = new ATM(new int[]{1, 2, 5, 10, 20, 50, 100, 200, 500}, 10, 100);
            atm2.initializeATM(Map.of(1, 200, 20, 30, 100, 10));

            Bank bank = new Bank();
            bank.addATM(atm1);
            bank.addATM(atm2);

            System.out.println("Total money in the bank network: " + bank.totalMoneyInNetwork());

            int withdrawalAmount = 230;
            Map<Integer, Integer> withdrawalNotes = atm1.withdrawMoney(withdrawalAmount);
            System.out.println("Withdrawal from ATM 1: " + withdrawalNotes);

            System.out.println("Total money in the bank network after withdrawal: " + bank.totalMoneyInNetwork());

        } catch (ATMException e) {
            System.out.println("Exception: " + e.getMessage());
        }
    }
}
