"use client";

import React from "react";
import { motion } from "framer-motion";
import Lottie from "lottie-react";
import loadingAnimation from "@/assets/animations/Loading.json";
import { cn } from "@/lib/utils";

interface LoadingProps {
    message?: string;
    className?: string;
}

export function Loading({
                            message = "Loading, please wait...",
                            className,
                        }: LoadingProps) {
    return (
        <div
            className={cn(
                "flex h-screen w-full items-center justify-center bg-background text-foreground",
                className
            )}
            role="status"
            aria-live="polite"
            aria-busy="true"
        >
            <motion.div
                initial={{ opacity: 0, scale: 0.95 }}
                animate={{ opacity: 1, scale: 1 }}
                transition={{ duration: 0.4, ease: "easeOut" }}
                className="flex flex-col items-center gap-4 px-4"
            >
                <Lottie
                    animationData={loadingAnimation}
                    loop
                    style={{ width: "60vw", height: "60vh" }}
                />
                <motion.p
                    className="text-center text-sm font-medium text-muted-foreground animate-pulse"
                    initial={{ opacity: 0 }}
                    animate={{ opacity: 1 }}
                    transition={{ delay: 0.4 }}
                >
                    {message}
                </motion.p>
            </motion.div>
        </div>
    );
}