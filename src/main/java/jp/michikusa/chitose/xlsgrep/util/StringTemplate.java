package jp.michikusa.chitose.xlsgrep.util;

import java.text.ParseException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

public class StringTemplate
{
    public StringTemplate(@NonNull CharSequence template)
        throws ParseException
    {
        this.template= lex(template.toString());
    }

    public String apply(Map<? super String, ? extends Object> data)
    {
        final StringBuilder buffer= new StringBuilder();
        for(final Token token : this.template)
        {
            if(token.type == Token.Type.PROPERTY)
            {
                final String value= data.containsKey(token.text)
                    ? "" + data.get(token.text)
                    : ""
                ;

                buffer.append(value);
            }
            else if(token.type == Token.Type.CHARACTER)
            {
                buffer.append(token.text);
            }
            else
            {
                throw new AssertionError();
            }
        }
        return buffer.toString();
    }

    @RequiredArgsConstructor
    @EqualsAndHashCode
    @ToString
    private static class Token
    {
        public static enum Type
        {
            CHARACTER,
            PROPERTY,
            ;
        }

        private final Type type;

        private final String text;
    }

    private static Iterable<Token> lex(@NonNull String in)
        throws ParseException
    {
        final List<Token> tokens= new LinkedList<>();
        int i= 0;
        while(i < in.length())
        {
            char ch= in.charAt(i);

            if(ch == '{')
            {
                final StringBuilder buffer= new StringBuilder();
                while(i < in.length())
                {
                    ++i;
                    ch= in.charAt(i);

                    if(ch == '}')
                    {
                        break;
                    }
                    else if(ch == '\\')
                    {
                        ++i;
                        ch= in.charAt(i);

                        if(ch == '{')
                        {
                            buffer.append('{');
                        }
                        else if(ch == '}')
                        {
                            buffer.append('}');
                        }
                        else if(ch == '\\')
                        {
                            buffer.append('\\');
                        }
                        else
                        {
                            throw new ParseException("エスケープ文字が不正です。", i);
                        }
                    }
                    else
                    {
                        buffer.append(ch);
                    }
                }
                tokens.add(new Token(Token.Type.PROPERTY, buffer.toString()));
            }
            else if(ch == '\\')
            {
                ++i;
                ch= in.charAt(i);

                if(ch == '{')
                {
                    tokens.add(new Token(Token.Type.CHARACTER, "{"));
                }
                else if(ch == '\\')
                {
                    tokens.add(new Token(Token.Type.CHARACTER, "\\"));
                }
                else if(ch == '}')
                {
                    tokens.add(new Token(Token.Type.CHARACTER, "}"));
                }
                else
                {
                    throw new ParseException("エスケープ文字が不正です。", i);
                }
            }
            else
            {
                tokens.add(new Token(Token.Type.CHARACTER, String.valueOf(ch)));
            }

            ++i;
        }
        return tokens;
    }

    private final Iterable<Token> template;
}
