import React from "react";

export function Divider() {
    return (
        <div className="relative flex items-center py-4">
            <div className="flex-grow border-t border-border" />
            <span className="mx-2 flex-shrink text-sm text-muted-foreground">Or continue with</span>
            <div className="flex-grow border-t border-border" />
        </div>
    )
}