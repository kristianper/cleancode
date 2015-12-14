import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * Created by eu2432 on 2015-10-21.
 */
public class EulerSolver {

	private static final int HIGH_CARD = 1;
	private static final int PAIR = 2;
	private static final int TWO_PAIR = 3;
	private static final int THREE_OF_A_KIND = 4;
	private static final int STRAIGHT = 5;
	private static final int FLUSH = 6;
	private static final int FULL_HOUSE = 7;
	private static final int FOUR_OF_A_KIND = 8;
	private static final int STRAIGHT_FLUSH = 9;
	private static final int ROYAL_FLUSH = 10;

	public static void main(String[] args) {
		List<Map<String, List<Card>>> rounds = new ArrayList<>();
		try {
			rounds = new EulerSolver().getRounds();
		} catch (IOException e) {
			e.printStackTrace();
		}
		int player1wins = 0;
		int player2wins = 0;
		for (Map<String, List<Card>> round : rounds) {
			if (isPlayer1Winner(round.get("player1"), round.get("player2"))) {
				player1wins++;
			} else {
				player2wins++;
			}
		}
		System.out.println("Player 1 wins: " + player1wins);
		System.out.println("Player 2 wins: " + player2wins);
	}

	private static boolean isPlayer1Winner(List<Card> player1, List<Card> player2) {
		// Calculate hand value
		int player1Hand = calculateT1(player1);
		int player2Hand = calculateT1(player2);
		if (player1Hand > player2Hand) {
			return true;
		}
		if (player2Hand > player1Hand) {
			return false;
		}
		// Calculate kickers
		Set<Integer> distinctValues = new HashSet<>();
		for (Card card : player1) {
			distinctValues.add(card.getValue());
		}
		for (int i = 1; i <= distinctValues.size(); i++) {
			if (calculateT2(player1, i) > calculateT2(player2, i)) {
				return true;
			} else if (calculateT2(player1, i) < calculateT2(player2, i)) {
				return false;
			}
		}
		return false;
	}

	private static int calculateT1(List<Card> player1) {
		if (isFlush(player1)) {
			if (isStraight(player1)) {
				return calculateT2(player1, 1) == 14 ? ROYAL_FLUSH : STRAIGHT_FLUSH;
			} else {
				return FLUSH;
			}
		}
		if (isStraight(player1)) {
			return STRAIGHT;
		}
		Map<Integer, List<Card>> cardsByValue = new HashMap<>();
		for (Card card : player1) {
			List<Card> cards = cardsByValue.get(card.getValue());
			if (cards == null) {
				cardsByValue.put(card.getValue(), Collections.singletonList(card));
			} else {
				List<Card> newList = new ArrayList<>();
				newList.addAll(cards);
				newList.add(card);
				cardsByValue.put(card.getValue(), newList);
			}
		}
		long noOfPairs = 0;
		long noOfTrips = 0;
		long noOfQuads = 0;
		for (List list : cardsByValue.values()) {
			if (list.size() == 2) {
				noOfPairs++;
			} else if (list.size() == 3) {
				noOfTrips++;
			} else if (list.size() == 4) {
				noOfQuads++;
			}
		}

		if (noOfQuads == 1) {
			return FOUR_OF_A_KIND;
		}
		if (noOfTrips == 1) {
			if (noOfPairs == 1) {
				return FULL_HOUSE;
			}
			return THREE_OF_A_KIND;
		}
		if (noOfPairs == 2) {
			return TWO_PAIR;
		}
		if (noOfPairs == 1) {
			return PAIR;
		}
		return HIGH_CARD;
	}

	private static int calculateT2(List<Card> cards, int numeral) {
		List<Integer> orderedKickers = new ArrayList<>();
		Map<Integer, Long> valueByNumber = new HashMap<>();
		for (Card card : cards) {
			Long number = valueByNumber.get(card.getValue());
			if (number == null) {
				valueByNumber.put(card.getValue(), 1l);
			} else {
				valueByNumber.put(card.getValue(), number + 1);
			}
		}

		for (int i = 4; i > 0; i--) {
			List<Integer> kickersByOccurances = new ArrayList<>();
			for (Integer kicker : valueByNumber.keySet()) {
				if (valueByNumber.get(kicker) == i) {
					kickersByOccurances.add(kicker);
				}
			}
			kickersByOccurances.sort(new Comparator<Integer>() {
				@Override public int compare(Integer o1, Integer o2) {
					return o2 - o1;
				}
			});
			orderedKickers.addAll(kickersByOccurances);
		}

		if (numeral > orderedKickers.size() || numeral < 1) {
			throw new IllegalArgumentException("Invalid kicker for hand.");
		}
		return orderedKickers.get(numeral - 1);
	}

	private static boolean isStraight(List<Card> cards) {
		boolean highStraight = true;
		boolean lowStraight = true;
		List<Integer> values = new ArrayList<>();
		for(Card card : cards) {
			values.add(card.getValue());
		}
		values.sort(new Comparator<Integer>() {
			@Override public int compare(Integer o1, Integer o2) {
				return o1 - o2;
			}
		});
		for (int i = 1; i < 5; i++) {
			if (values.get(i) - values.get(i - 1) != 1) {
				highStraight = false;
				break;
			}
		}
		if (values.contains(14)) {
			values.clear();
			for(Card card : cards) {
				if (card.getValue() == 14) {
					values.add(1);
				} else {
					values.add(card.getValue());
				}
			}
			values.sort(new Comparator<Integer>() {
				@Override public int compare(Integer o1, Integer o2) {
					return o1 - o2;
				}
			});
			for (int i = 1; i < 5; i++) {
				if (values.get(i) - values.get(i - 1) != 1) {
					lowStraight = false;
					break;
				}
			}
		} else {
			return highStraight;
		}
		return highStraight || lowStraight;
	}

	private static boolean isFlush(List<Card> cards) {
		Set<Character> suits = new HashSet<>();
		for (Card card : cards) {
			suits.add(card.getSuit());
		}
		return suits.size() == 1;
	}

	private List<Map<String, List<Card>>> getRounds() throws IOException {
		List<Map<String, List<Card>>> rounds = new ArrayList<>();
		try (BufferedReader br = new BufferedReader(new FileReader(getClass().getClassLoader().getResource("p054_poker.txt").getFile()))) {
			String line;
			while ((line = br.readLine()) != null) {
				rounds.add(processLine(line));
			}
			return rounds;
		}
	}

	private Map<String, List<Card>> processLine(String line) {
		Map<String, List<Card>> pokerRound = new HashMap<>();
		pokerRound.put("player1", Arrays.asList(
				new Card(mapToInt(line.charAt(0)), line.charAt(1)),
				new Card(mapToInt(line.charAt(3)), line.charAt(4)),
				new Card(mapToInt(line.charAt(6)), line.charAt(7)),
				new Card(mapToInt(line.charAt(9)), line.charAt(10)),
				new Card(mapToInt(line.charAt(12)), line.charAt(13))));
		pokerRound.put("player2", Arrays.asList(
				new Card(mapToInt(line.charAt(15)), line.charAt(16)),
				new Card(mapToInt(line.charAt(18)), line.charAt(19)),
				new Card(mapToInt(line.charAt(21)), line.charAt(22)),
				new Card(mapToInt(line.charAt(24)), line.charAt(25)),
				new Card(mapToInt(line.charAt(27)), line.charAt(28))));
		return pokerRound;
	}

	private int mapToInt(char c) {
		if (c == 'T') {
			return 10;
		}
		if (c == 'J') {
			return 11;
		}
		if (c == 'Q') {
			return 12;
		}
		if (c == 'K') {
			return 13;
		}
		if (c == 'A') {
			return 14;
		}
		return c - 48;
	}


	private class Card {

		private Integer value;
		private char suit;

		public Card(Integer value, char suit) {
			this.value = value;
			this.suit = suit;
		}

		public Integer getValue() {
			return value;
		}

		public char getSuit() {
			return suit;
		}
	}
}
