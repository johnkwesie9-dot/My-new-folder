package compiler;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.junit.jupiter.api.Test;

/** OWNER: Unit test. One focused test per scanner. */
class LexerUnitTest {
    private static List<Token.Type> types(String src) {
        return new Lexer(src).scanAll().stream().map(Token::type).toList();
    }

    @Test
    void keywordVsIdentifier() {
        assertEquals(Token.Type.IDENTIFIER, types("integer").get(0));
        assertEquals(Token.Type.KW_WHILE, types("while").get(0));
    }

    @Test
    void intAndFloat() {
        assertEquals(List.of(Token.Type.INT_LITERAL, Token.Type.FLOAT_LITERAL, Token.Type.EOF), types("42 3.14"));
    }

    @Test
    void twoCharOperators() {
        assertEquals(List.of(Token.Type.LE, Token.Type.EQ, Token.Type.NEQ, Token.Type.AND, Token.Type.OR, Token.Type.EOF),
                types("<= == != && ||"));
    }

    @Test
    void loneAmpersandIsError() {
        Lexer lx = new Lexer("a & b");
        lx.scanAll();
        assertTrue(lx.hadError());
    }

    @Test
    void delimiters() {
        assertEquals(List.of(Token.Type.LPAREN, Token.Type.RPAREN, Token.Type.LBRACE, Token.Type.RBRACE,
                Token.Type.SEMICOLON, Token.Type.COMMA, Token.Type.EOF), types("(){};,"));
    }

    @Test
    void stringWithEscape() {
        List<Token> t = new Lexer("\"a\\nb\"").scanAll();
        assertEquals(Token.Type.STRING_LITERAL, t.get(0).type());
        assertEquals("a\nb", t.get(0).lexeme());
    }

    @Test
    void unterminatedStringIsError() {
        Lexer lx = new Lexer("\"oops");
        lx.scanAll();
        assertTrue(lx.hadError());
    }

    @Test
    void commentsAreSkippedAndSlashStillWorks() {
        assertEquals(List.of(Token.Type.IDENTIFIER, Token.Type.SLASH, Token.Type.IDENTIFIER, Token.Type.EOF),
                types("// hi\n/* block */ a / b"));
    }

    @Test
    void unterminatedBlockCommentIsError() {
        Lexer lx = new Lexer("/* never closed");
        lx.scanAll();
        assertTrue(lx.hadError());
    }

    @Test
    void tracksLineAndColumn() {
        List<Token> t = new Lexer("a\n  b").scanAll();
        assertEquals(2, t.get(1).line());
        assertEquals(3, t.get(1).column());
    }

    @Test
    void recoversAfterBadCharacter() {
        Lexer lx = new Lexer("int @ x;");
        List<Token> t = lx.scanAll();
        assertEquals(1, lx.errors().size());
        assertEquals(Token.Type.IDENTIFIER, t.get(1).type());
    }
}
