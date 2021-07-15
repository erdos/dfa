package dev.erdos.automata

import spock.lang.Specification
import spock.lang.Unroll

class NDFATest extends Specification {

	@Unroll
	def 'test #word'() {
		expect:
			def automata = NDFA.levenshtein('abraham', 2)
			automata.test(word) == expected
		where:
			expected | word
			true     | 'abraham' // d = 0
			true     | 'braham' // d = 1
			true     | 'abraha' // d = 1
			true     | 'abrah' // d = 2
			true     | 'xxabraham' // d = 2
			true     | 'xabrahamx'
			true     | 'araha' // d = 2
			true     | 'aabraham' // d = 1
			false    | ''
			false    | 'abra' // d = 3
			false    | 'brah' // d = 3
			false    | 'abrahamabraham'
	}

	@Unroll
	def 'test #word with d=1'() {
		expect:
			def automata = NDFA.levenshtein('janos', 1)
			automata.test(word) == expected
		where:
			expected | word
			true     | 'anos'
			true     | 'jjanos'
			true     | 'aanos'
			true     | 'anos'
			true     | 'xanos'
			false    | 'jjjanos'
			false    | 'xadfg'
			false    | 'jan'
	}

	@Unroll
	def 'levenshtein for exact matching'() {
		expect:
			def automata = NDFA.levenshtein('abraham', 0)
			automata.test('abraham')
			!automata.test('abraha')
			!automata.test('')
			!automata.test('xxdsdasdsd')
			automata.alphabet() == ['a', 'b', 'h', 'm', 'r'] as Set<Character>
			automata.size() == 8
	}
}
