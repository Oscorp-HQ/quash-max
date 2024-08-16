# Contributing to Quash

If you plan to contribute to Quash, please take a moment to feel awesome 🚀 People like you are what open source is about ♥. Any contributions, no matter how big or small, are highly appreciated.

# Join the Quash Community

Thinking about contributing to Quash? Awesome! 🌟 Your enthusiasm and willingness to participate in open source projects make a real difference. Whether you're a seasoned developer or just starting out, your contributions—big or small—are incredibly valuable 🚀♥.

### Before Getting Started

- Before jumping into a PR be sure to search [existing PRs](https://github.com/Oscorp-HQ/quash-max/pulls) or [issues](https://github.com/Oscorp-HQ/quash-max/issues) for an open or closed item that relates to your submission.
- Select an issue from [here](https://github.com/Oscorp-HQ/quash-max/issues) or create a new one
- Consider the results from the discussion on the issue
- Ensure that you follow the [Code of Conduct](https://github.com/dhairya-quash/TEST-REPO/blob/main/Code%20Of%20Conduct.md) to ensure we can accept your contributions.

### Taking Issues

Before taking an issue, ensure that:

- The issue has been assigned the public label
- The issue is clearly defined and understood
- No one has been assigned to the issue
- No one has expressed intention to work on it

You can then:

- Comment on the issue with your intention to work on it
- Begin work on the issue

Always feel free to ask questions or seek clarification on the issue.

### Developing

The development branch is main. All pull requests should be made against this branch. If you need help getting started, join us on [Discord](https://discord.com/invite/Nxbe8F6aqw).

1. Fork this repository to your own GitHub account and then clone it to your local device.
2. Move to your workspace:
   ```bash
   cd your-workspace
   ```
3. Move to the directory of your choice and start making changes.
    ```bash
    # For frontend
    cd quash-max/frontend
  
    # For backend
    cd quash-max/backend
  
    # For SDK
    cd quash-max/android
    ```
4. Create a new branch:

- Create a new branch (include the issue id and something readable):

  ```bash
  git checkout -b feat/quash-some-feature
  ```
5. See the [Developer Setup](https://github.com/dhairya-quash/TEST-REPO) for more setup details.

### Building

> Note Please ensure you can make a full production build before pushing code or creating PRs.
> You can build the the respective components with:

_Frontend_:

```bash
npm run build
```

_Backend_:

```java
mvn clean package
```

_SDK_:

In your IDE, navigate to the tools tab and click `clean project` and then `make project`
