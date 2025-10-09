import os
import string

from bitarray import bitarray


def rice_encode(numbers, k):
    """Encodes a list of integers using Rice encoding with parameter k."""
    encoded_bits = bitarray()
    for number in numbers:
        quotient = number >> k  # Integer division by 2^k
        remainder = number & ((1 << k) - 1)  # Modulus 2^k

        # Encode quotient in unary (e.g., 3 becomes "1110")
        encoded_bits.extend('1' * quotient + '0')

        # Encode remainder in binary
        remainder_bits = f"{remainder:0{k}b}"
        encoded_bits.extend(remainder_bits)
    return encoded_bits


def rice_decode(encoded_bits, k):
    """Decodes a Rice-encoded bitarray back into the list of integers."""
    numbers = []
    i = 0
    while i < len(encoded_bits):
        # Decode unary quotient
        quotient = 0
        while encoded_bits[i] == 1:
            quotient += 1
            i += 1
        i += 1  # Skip the '0' that ends the unary encoding

        # Decode binary remainder
        remainder = int(encoded_bits[i:i + k].to01(), 2)
        i += k

        # Reconstruct the original number
        number = (quotient << k) + remainder
        numbers.append(number)

    return numbers



def count_word_sizes_by_letter(directory_path):
    # Dictionary to store the total word sizes for each letter
    letter_word_size = {letter: 0 for letter in string.ascii_uppercase}

    # Loop through each file in the specified directory
    for filename in os.listdir(directory_path):
        file_path = os.path.join(directory_path, filename)

        # Ensure we only process files (not directories)
        if os.path.isfile(file_path):
            with open(file_path, 'r') as file:
                for line in file:
                    words = line.split()
                    for word in words:
                        # Check each letter for the word and update count
                        unique_letters = set(word.upper())  # Make case insensitive
                        for letter in unique_letters:
                            if letter in letter_word_size:
                                letter_word_size[letter] += len(word)

    return letter_word_size


def calculate_deltas(numbers):
    """Calculates the deltas (differences) between consecutive numbers in a sorted set."""
    # Sort the numbers to ensure deltas are calculated correctly
    sorted_numbers = sorted(numbers)

    # Calculate the deltas between consecutive numbers
    deltas = [sorted_numbers[i] - sorted_numbers[i - 1] for i in range(1, len(sorted_numbers))]

    return deltas

def get_words(file_path):
    letter_word_size = {letter: set() for letter in string.ascii_uppercase}

    # Loop through each file in the specified directory

    # Ensure we only process files (not directories)
    if os.path.isfile(file_path):
        with open(file_path, 'r', encoding="utf8") as file:
            for line in file:
                words = line.split()
                for word in words:
                    # Check each letter for the word and update count
                    if word.isalpha():
                        letter = word.upper()[0]
                        letter_word_size[letter].add(len(word))

    return letter_word_size


all_sizes = []
# Example usage:
for filename in os.listdir('z:\\wiki\\output_pages'):
    file_path = os.path.join('z:\\wiki\\output_pages', filename)
    try:
        result = get_words(file_path)
        totalsize = 0
        for letter, size in result.items():
                delta = calculate_deltas(size)
                totalsize = totalsize + len(rice_encode(delta, 1)) / 8
                #print(rice_encode(delta, 1))
                #print(size)
                #print(f"Total word size for words containing '{letter}': {size}")

        if totalsize > 0:
            all_sizes.append(totalsize)
    except:
        print("Exception")


    print(all_sizes)