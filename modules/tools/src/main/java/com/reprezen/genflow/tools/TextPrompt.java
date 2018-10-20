package com.reprezen.genflow.tools;

import static java.util.stream.Collectors.toList;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class TextPrompt {
	BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

	public Optional<String> getString(String prompt, Function<String, Optional<String>> function) throws IOException {
		while (true) {
			System.out.print(prompt);
			String line = in.readLine();
			if (line == null || line.trim().length() == 0) {
				return Optional.empty();
			} else {
				line = line.trim();
				Optional<String> error = function != null ? function.apply(line) : Optional.empty();
				if (error.isPresent()) {
					System.out.println(error.get());
				} else {
					return Optional.of(line);
				}
			}
		}
	}

	public Optional<Integer> getChoice(String prompt, List<String> choices) throws IOException {
		while (true) {
			printChoices(choices);
			Optional<String> response = getString(prompt, (s) -> {
				if (s.matches("^[0-9]+$")) {
					int n = Integer.parseInt(s);
					if (n > 0 && n <= choices.size()) {
						return Optional.empty();
					}
				}
				printChoices(choices);
				return Optional.of(String.format("Please enter an integer from 1 to %d", choices.size()));
			});
			return response.map(s -> Integer.parseInt(s) - 1);
		}
	}

	private void printChoices(List<String> choices) {
		for (int i = 0; i < choices.size(); i++) {
			System.out.printf("[%d] %s\n", i + 1, choices.get(i));
		}
	}

	public <E extends Enum<E> & Supplier<String>> Optional<E> getChoice(String prompt, Class<E> enumClass)
			throws IOException {
		E[] members = enumClass.getEnumConstants();
		List<String> choices = Stream.of(members).map(m -> m.get()).collect(toList());
		Optional<Integer> choice = getChoice(prompt, choices);
		return choice.map(i -> members[i]);
	}
}
