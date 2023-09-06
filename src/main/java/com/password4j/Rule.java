/*
 *  (C) Copyright 2023 Password4j (http://password4j.com/).
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package com.password4j;

public interface Rule
{

    Rule lowerCaseLetters = new SymbolBasedRule(Symbols.LOWERCASE_LETTERS_CHARS);

    Rule UPPERCASE_LETTERS = new SymbolBasedRule(Symbols.UPPERCASE_LETTERS_CHARS);

    Rule letters = new SymbolBasedRule(Symbols.LETTERS.toCharArray());

    Rule digits = new SymbolBasedRule(Symbols.DIGITS_CHARS);

    Rule specials = new SymbolBasedRule(Symbols.SPECIALS_CHARACTERS_CHARS);

    Rule alphanumeric = new SymbolBasedRule(Symbols.ALPHANUMERIC.toCharArray());

    Rule printable = new SymbolBasedRule(Symbols.PRINTABLE.toCharArray());

    Rule noRepetitions = new LogicBasedRule()
    {
        @Override
        public String apply(PasswordGenerator generator, String generatedSoFar)
        {
            if (generatedSoFar.length() == 0)
            {
                return "";
            }
            else if (generatedSoFar.length() == 1)
            {
                return generatedSoFar;
            }

            char[] result = generatedSoFar.toCharArray();

            for (int i = 1; i < generatedSoFar.length(); i++)
            {
                char previous = result[i - 1];

                while (previous == result[i])
                {
                    result[i] = generator.pickNewChar();
                }
            }

            return new String(result);

        }
    };


    Rule noConsecutives = new LogicBasedRule()
    {
        @Override
        public String apply(PasswordGenerator generator, String generatedSoFar)
        {
            if (generatedSoFar.length() == 0)
            {
                return "";
            }
            else if (generatedSoFar.length() == 1)
            {
                return generatedSoFar;
            }

            char[] result = generatedSoFar.toCharArray();

            for (int i = 1; i < generatedSoFar.length(); i++)
            {
                char previous = result[i - 1];

                while (Character.isDigit(previous) //
                        && Character.isDigit(result[i]) //
                        && Math.abs(previous - result[i]) == 1)
                {
                    result[i] = generator.pickNewChar();
                }
            }

            return new String(result);
        }
    };


}
