import React from 'react';
import { render, screen, fireEvent, waitFor, act } from '@testing-library/react';
import ChangePasswordPage from '@/app/change-password/page'; // Adjust path if needed
import { AuthContext, AuthContextType } from '@/contexts/auth-context';
import { useRouter, useSearchParams } from 'next/navigation';
import type { AxiosInstance } from 'axios';

jest.mock('next/navigation', () => ({
    useRouter: jest.fn(),
    useSearchParams: jest.fn(),
}));

jest.mock('@/components/layouts/AuthLayout', () => ({
    __esModule: true,
    default: ({ children }: { children: React.ReactNode }) => <div>{children}</div>,
}));

jest.mock('@/components/layouts/LoadingButton', () => ({
    __esModule: true,
    default: (props: {
        onClick?: () => void;
        children: React.ReactNode;
        loading: boolean;
    }) => (
        <button onClick={props.onClick} disabled={props.loading}>
            {props.loading ? 'Loading...' : props.children}
        </button>
    ),
}));

// Utility for mock search params
const mockSearchParams = {
    get: jest.fn(),
};

describe('ChangePasswordPage', () => {
    const push = jest.fn();
    const post = jest.fn();

    beforeEach(() => {
        jest.clearAllMocks();
        (useRouter as jest.Mock).mockReturnValue({ push });
        (useSearchParams as jest.Mock).mockReturnValue(mockSearchParams);
    });

    const renderPage = () => {
        const mockContext: AuthContextType = {
            isAuthenticated: true,
            user: null,
            login: jest.fn(),
            logout: jest.fn(),
            api: {
                post: post as AxiosInstance['post'],
            } as AxiosInstance,
            loading: false,
        };
        return render(
            <AuthContext.Provider value={mockContext}>
                <ChangePasswordPage />
            </AuthContext.Provider>
        );
    }

    it('pre-fills email from search params', () => {
        mockSearchParams.get.mockReturnValue('test@example.com');
        renderPage();

        expect(screen.getByLabelText(/email address/i)).toHaveValue('test@example.com');
    });

    it('shows error when email is empty', async () => {
        mockSearchParams.get.mockReturnValue('');
        renderPage();

        fireEvent.change(screen.getByLabelText(/email address/i), {
            target: { value: '' },
        });

        fireEvent.click(screen.getByText(/request change password/i));
        expect(await screen.findByText(/email is required/i)).toBeInTheDocument();
    });

    it('shows error when email is invalid', async () => {
        mockSearchParams.get.mockReturnValue('');
        renderPage();

        fireEvent.change(screen.getByLabelText(/email address/i), {
            target: { value: 'invalid' },
        });

        fireEvent.click(screen.getByText(/request change password/i));
        expect(await screen.findByText(/valid email address/i)).toBeInTheDocument();
    });

    it('calls API and shows success message', async () => {
        mockSearchParams.get.mockReturnValue('valid@example.com');
        post.mockResolvedValue({ status: 200, data: { message: 'Email sent!' } });

        renderPage();

        fireEvent.click(screen.getByText(/request change password/i));

        expect(await screen.findByText(/email sent/i)).toBeInTheDocument();
        expect(screen.getByText(/redirecting to login page/i)).toBeInTheDocument();
    });

    it('handles API failure and shows error', async () => {
        mockSearchParams.get.mockReturnValue('fail@example.com');
        post.mockRejectedValue({ response: { data: { message: 'Server error' } } });

        renderPage();

        fireEvent.click(screen.getByText(/request change password/i));
        expect(await screen.findByText(/server error/i)).toBeInTheDocument();
    });

    it('redirects to login after countdown', async () => {
        jest.useFakeTimers(); // Step 1: Enable fake timers

        mockSearchParams.get.mockReturnValue('valid@example.com');
        post.mockResolvedValue({ status: 200, data: { message: 'Done' } });

        renderPage();

        // Step 2: Trigger the form submit
        fireEvent.click(screen.getByText(/request change password/i));

        // Step 3: Wait for success UI
        await waitFor(() =>
            expect(screen.getByText(/redirecting to login page/i)).toBeInTheDocument()
        );

        // Step 4: Advance timers repeatedly for countdown to complete
        for (let i = 0; i < 10; i++) {
            await act(async () => {
                jest.advanceTimersByTime(1000); // simulate 1 second
                await Promise.resolve(); // flush pending microtasks
            });
        }

        // Step 5: Assert router.push was called
        expect(push).toHaveBeenCalledWith('/login');

        jest.useRealTimers(); // cleanup
    });
});