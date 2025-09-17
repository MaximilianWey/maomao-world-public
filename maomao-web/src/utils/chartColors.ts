export function getCssVariable(varName: string): string {
    return getComputedStyle(document.documentElement).getPropertyValue(varName).trim();
}

export const chartColors = {
    background: [
        getCssVariable('--accent-1'),
        getCssVariable('--accent-2'),
        getCssVariable('--accent-success'),
        getCssVariable('--youtube-main'),
        getCssVariable('--spotify-main'),
        getCssVariable('--soundcloud-main'),
    ],
    border: [
        getCssVariable('--accent-1-hover'),
        getCssVariable('--accent-2-hover'),
        getCssVariable('--accent-success'),
        getCssVariable('--youtube-highlight'),
        getCssVariable('--spotify-highlight'),
        getCssVariable('--soundcloud-highlight'),
    ],
    text: getCssVariable('--text-primary'),
    grid: getCssVariable('--border-color-darker'),
};

window.matchMedia('(prefers-color-scheme: dark)').addEventListener('change', () => {
    // Re-evaluate colors when theme changes
    chartColors.background = [
        getCssVariable('--accent-1'),
        getCssVariable('--accent-2'),
        getCssVariable('--accent-success'),
        getCssVariable('--youtube-main'),
        getCssVariable('--spotify-main'),
        getCssVariable('--soundcloud-main'),
    ];
    chartColors.border = [
        getCssVariable('--accent-1-hover'),
        getCssVariable('--accent-2-hover'),
        getCssVariable('--accent-success'),
        getCssVariable('--youtube-highlight'),
        getCssVariable('--spotify-highlight'),
        getCssVariable('--soundcloud-highlight'),
    ];
    chartColors.text = getCssVariable('--text-primary');
    chartColors.grid = getCssVariable('--border-color-darker');
});