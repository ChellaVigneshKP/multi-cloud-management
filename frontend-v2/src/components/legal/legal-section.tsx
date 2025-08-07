import React from "react"

export const LegalSection = ({
                                 title,
                                 children,
                             }: {
    title: string
    children: React.ReactNode
}) => (
    <section className="space-y-2">
        <h2 className="text-lg md:text-xl font-semibold">{title}</h2>
        {children}
    </section>
)
