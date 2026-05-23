import java.util.Random;
import java.util.Scanner;
import java.util.ArrayList;

public class Blackjack {
    public static void main(String[] args) {
        int gameSeed = Integer.parseInt(args[0]);
        int numParticipants = Integer.parseInt(args[1]);

        Deck cardDeck = new Deck();
        cardDeck.shuffle(gameSeed);

        Player user = new Player();
        ArrayList<Computer> aiList = new ArrayList<>();
        for (int i = 0; i < numParticipants - 1; i++) {
            aiList.add(new Computer());
        }
        House dealer = new House();

        ArrayList<Hand> playOrder = new ArrayList<>();
        playOrder.add(user);
        for (Computer c : aiList) playOrder.add(c);
        playOrder.add(dealer);

        for (int round = 0; round < 2; round++) {
            for (Hand h : playOrder) {
                h.addCard(cardDeck.dealCard());
            }
        }

        System.out.println("House: " + dealer.getInitialView());
        System.out.println("Player1: " + user.toString());
        for (int i = 0; i < aiList.size(); i++) {
            System.out.println("Player" + (i + 2) + ": " + aiList.get(i).toString());
        }

        if (dealer.getScore() == 21) {
            System.out.println();
            displayFinal(dealer, user, aiList);
            return;
        }

        Scanner sc = new Scanner(System.in);
        user.runTurn(sc, cardDeck);

        java.util.Random aiRand = new java.util.Random();
        Thread[] aiThreads = new Thread[aiList.size()];
        for (int i = 0; i < aiList.size(); i++) {
            aiThreads[i] = new Thread(new Computer(aiRand, cardDeck, i + 2));
            aiThreads[i].start();
        }
        for (Thread t : aiThreads) {
            try {
                t.join(); 
            } catch (InterruptedException e) {
                System.out.println("Thread execution interrupted");
            }
        }
        dealer.runTurn(cardDeck);
        displayFinal(dealer, user, aiList);
    }

    private static void displayFinal(House h, Player p1, ArrayList<Computer> comps) {
        System.out.println("\n--- Game Results ---");
        System.out.println("House: " + h.toString());
        judgeResult("Player1", p1, h);
        for (int i = 0; i < comps.size(); i++) {
            judgeResult("Player" + (i + 2), comps.get(i), h);
        }
    }

    private static void judgeResult(String name, Hand p, House h) {
        int ps = p.getScore();
        int hs = h.getScore();
        String tag;
        if (ps > 21) tag = "[Lose]";
        else if (hs > 21 || ps > hs) tag = "[Win]";
        else if (ps < hs) tag = "[Lose]";
        else tag = "[Draw]";
        System.out.print(tag + " " + name + ": " + p.toString());
        if (ps > 21) System.out.print(" - Bust!");
        System.out.println();
    }
}

class Card {
    int rank;
    int suit;
    public Card() {}
    public Card(int theValue, int theSuit) {
        this.rank = theValue;
        this.suit = theSuit;
    }
    public String toString() {
        String[] ranks = {"", "A", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K"};
        String[] suits = {"c", "h", "d", "s"};
        return ranks[rank] + suits[suit];
    }
}

class Deck {
    private Card[] cards = new Card[52];
    private int cardsUsed = 0;
    
    public Deck() {
        int pos = 0;
        for (int s = 0; s < 4; s++) {
            for (int v = 1; v <= 13; v++) {
                cards[pos++] = new Card(v, s);
            }
        }
    }
    
    public void shuffle() {
    java.util.Random random = new java.util.Random();
    for (int i = cards.length - 1; i > 0; i--) {
        int rand = random.nextInt(i + 1);
        Card temp = cards[i];
        cards[i] = cards[rand];
        cards[rand] = temp;
    }
    cardsUsed = 0;
}
    public synchronized Card dealCard() {
        if (cardsUsed == cards.length) throw new IllegalStateException();
        return cards[cardsUsed++];
    }
}

class Hand {
    protected ArrayList<Card> handCards = new ArrayList<>();
    public void addCard(Card c) { handCards.add(c); }
    public int getScore() {
        int total = 0;
        int aces = 0;
        for (Card c : handCards) {
            if (c.rank == 1) {
                aces++;
                total += 11;
            } else if (c.rank >= 10) {
                total += 10;
            } else {
                total += c.rank;
            }
        }
        while (total > 21 && aces > 0) {
            total -= 10;
            aces--;
        }
        return total;
    }
    public String toString() {
        StringBuilder res = new StringBuilder();
        for (int i = 0; i < handCards.size(); i++) {
            res.append(handCards.get(i).toString());
            if (i < handCards.size() - 1) res.append(", ");
        }
        return res.toString() + " (" + getScore() + ")";
    }
}

class Computer extends Hand implements Runnable {
    private java.util.Random r;
    private Deck d;
    private int id;
    public Computer(java.util.Random r, Deck d, int id) {
        this.r = r;
        this.d = d;
        this.id = id;
    }
    @Override
    public void run() {
        runTurn(r, d, id);
    }
    public void runTurn(java.util.Random r, Deck d, int id) {
        System.out.println("\n--- Player" + id + " turn ---");
        while (getScore() < 21) {
            System.out.println("Player" + id + ": " + this.toString());
            int s = getScore();
            boolean hit = (s < 14) || (s <= 17 && r.nextInt(2) == 1);
            if (hit) {
                System.out.println("Hit");
                addCard(d.dealCard());
                if (getScore() > 21) {
                    System.out.println("Player" + id + ": " + this.toString() + " - Bust!");
                    break;
                }
            } else {
                System.out.println("Stand\nPlayer" + id + ": " + this.toString());
                break;
            }
        }
    }
}
class Player extends Hand {
    public void runTurn(Scanner s, Deck d) {
        System.out.println("\n--- Player1 turn ---");
        while (getScore() < 21) {
            System.out.println("Player1: " + this.toString());
            if (s.next().equalsIgnoreCase("Hit")) {
                System.out.println("Hit");
                addCard(d.dealCard());
                if (getScore() > 21) {
                    System.out.println("Player1: " + this.toString() + " - Bust!");
                    break;
                }
            } else {
                System.out.println("Stand\nPlayer1: " + this.toString());
                break;
            }
        }
    }
}

class House extends Hand {
    public void runTurn(Deck d) {
        System.out.println("\n--- House turn ---");
        while (getScore() <= 16) {
            System.out.println("House: " + this.toString() + "\nHit");
            addCard(d.dealCard());
            if (getScore() > 21) {
                System.out.println("House: " + this.toString() + " - Bust!");
                return;
            }
        }
        System.out.println("House: " + this.toString() + "\nStand\nHouse: " + this.toString());
    }
    public String getInitialView() {
        if (handCards.size() < 2) return "";
        return "HIDDEN, " + handCards.get(1).toString();
    }
}