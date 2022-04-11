package geektime.tdd.args;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class SortTest {
    @Test
    public void should_sort_array() {
        assertArrayEquals(new int[]{9, 7, 4, 3, 1}, Sort.bubble(4, 7, 3, 1, 9));
    }

    @Test
    public void should_sort_smaller_array() {
        assertArrayEquals(new int[]{7, 4}, Sort.bubble(4, 7));
    }

    @Test
    public void should_sort() {
        Sort.SwapFunc func = mock(Sort.SwapFunc.class);
        Sort.bubble(func, 4, 7);
        verify(func).swap(eq(0), eq(1), any());
    }

    @Test
    public void should_sort_3_values() {
        Sort.SwapFunc func = mock(Sort.SwapFunc.class);
        Sort.bubble(func,  4, 1, 7);

        verify(func).swap(eq(0), eq(2), any());
        verify(func).swap(eq(1), eq(2), any());
    }
}
